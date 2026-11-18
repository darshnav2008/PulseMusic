package com.pulsemusic.app

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.pulsemusic.app.data.AudiusRepository
import com.pulsemusic.app.data.LocalMusicRepository
import com.pulsemusic.app.model.Track
import com.pulsemusic.app.model.TrackSource
import com.pulsemusic.app.model.YouTubePlaylistItem
import com.pulsemusic.app.player.ZeroLatencyAudioEngine
import com.pulsemusic.app.youtube.YouTubeConnectManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class LibraryTab(val label: String) {
    FREE_AD_FREE("Free Ad-Free"),
    YOUTUBE_MUSIC("YouTube Music"),
    LOCAL("On Device")
}

data class MainUiState(
    val selectedTab: LibraryTab = LibraryTab.FREE_AD_FREE,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val audiusTracks: List<Track> = emptyList(),
    val localTracks: List<Track> = emptyList(),
    val youtubeTracks: List<Track> = emptyList(),
    val youtubePlaylists: List<YouTubePlaylistItem> = emptyList(),
    val youtubeAccountEmail: String? = null,
    val youtubeStatusMessage: String? = null,
    val currentQueue: List<Track> = emptyList(),
    val currentTrack: Track? = null,
    val activeYouTubeTrack: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val showNowPlaying: Boolean = false
) {
    val visibleTracks: List<Track>
        get() = when (selectedTab) {
            LibraryTab.FREE_AD_FREE -> audiusTracks
            LibraryTab.YOUTUBE_MUSIC -> youtubeTracks
            LibraryTab.LOCAL -> if (searchQuery.isBlank()) localTracks else localTracks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true)
            }
        }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = ZeroLatencyAudioEngine(application)
    private val audiusRepo = AudiusRepository()
    private val localRepo = LocalMusicRepository(application)
    val youtubeManager = YouTubeConnectManager(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        attachPlayerListener()
        startProgressTicker()
        loadInitialData()
        checkExistingYouTubeSession()
    }

    private fun attachPlayerListener() {
        engine.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = engine.player.currentMediaItemIndex
                val queue = _uiState.value.currentQueue
                val track = queue.getOrNull(index)
                if (track != null) {
                    _uiState.update {
                        it.copy(
                            currentTrack = track,
                            durationMs = engine.player.duration.coerceAtLeast(track.durationMs)
                        )
                    }
                    // Pre-cache the next track immediately for zero-latency switching
                    engine.preloadNextTrack(index)
                }
            }
        })
    }

    private fun startProgressTicker() {
        viewModelScope.launch {
            while (isActive) {
                if (engine.player.isPlaying) {
                    _uiState.update {
                        it.copy(
                            positionMs = engine.player.currentPosition.coerceAtLeast(0L),
                            durationMs = engine.player.duration.coerceAtLeast(1L)
                        )
                    }
                }
                delay(250L)
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val trending = audiusRepo.getTrendingTracks()
            val local = localRepo.loadLocalSongs()
            _uiState.update {
                it.copy(
                    audiusTracks = trending,
                    localTracks = local,
                    isLoading = false
                )
            }
        }
    }

    fun refreshLocalLibrary() {
        viewModelScope.launch {
            val local = localRepo.loadLocalSongs()
            _uiState.update { it.copy(localTracks = local) }
        }
    }

    private fun checkExistingYouTubeSession() {
        val account = youtubeManager.getSignedInAccount()
        if (account != null) {
            _uiState.update { it.copy(youtubeAccountEmail = account.email) }
            loadYouTubePlaylists()
        }
    }

    fun getYouTubeSignInIntent(): Intent = youtubeManager.getSignInIntent()

    fun onYouTubeSignInResult() {
        val account = youtubeManager.getSignedInAccount()
        if (account != null) {
            _uiState.update {
                it.copy(
                    youtubeAccountEmail = account.email,
                    youtubeStatusMessage = null
                )
            }
            loadYouTubePlaylists()
        } else {
            _uiState.update {
                it.copy(youtubeStatusMessage = "Sign-in cancelled or Google Cloud OAuth not configured yet.")
            }
        }
    }

    fun disconnectYouTube() {
        youtubeManager.signOut {
            _uiState.update {
                it.copy(
                    youtubeAccountEmail = null,
                    youtubePlaylists = emptyList(),
                    youtubeTracks = emptyList(),
                    activeYouTubeTrack = null,
                    youtubeStatusMessage = null
                )
            }
        }
    }

    private fun loadYouTubePlaylists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            youtubeManager.fetchMyPlaylists()
                .onSuccess { playlists ->
                    _uiState.update {
                        it.copy(
                            youtubePlaylists = playlists,
                            youtubeStatusMessage = if (playlists.isEmpty()) "No playlists found on this account." else null,
                            isLoading = false
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            youtubeStatusMessage = err.message ?: "Failed to load playlists",
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun openYouTubePlaylist(playlist: YouTubePlaylistItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            youtubeManager.fetchPlaylistTracks(playlist.id)
                .onSuccess { tracks ->
                    _uiState.update {
                        it.copy(youtubeTracks = tracks, youtubeStatusMessage = null, isLoading = false)
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(youtubeStatusMessage = err.message, isLoading = false)
                    }
                }
        }
    }

    fun selectTab(tab: LibraryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun performSearch() {
        val query = _uiState.value.searchQuery.trim()
        when (_uiState.value.selectedTab) {
            LibraryTab.FREE_AD_FREE -> viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val results = audiusRepo.search(query)
                _uiState.update { it.copy(audiusTracks = results, isLoading = false) }
            }
            LibraryTab.YOUTUBE_MUSIC -> viewModelScope.launch {
                if (query.isBlank()) return@launch
                _uiState.update { it.copy(isLoading = true) }
                youtubeManager.searchYouTubeMusic(query)
                    .onSuccess { tracks ->
                        _uiState.update {
                            it.copy(youtubeTracks = tracks, youtubeStatusMessage = null, isLoading = false)
                        }
                    }
                    .onFailure { err ->
                        _uiState.update {
                            it.copy(youtubeStatusMessage = err.message, isLoading = false)
                        }
                    }
            }
            LibraryTab.LOCAL -> Unit // Filtered live in visibleTracks
        }
    }

    fun playTrack(tracks: List<Track>, index: Int) {
        val selected = tracks.getOrNull(index) ?: return
        if (selected.source == TrackSource.YOUTUBE_OFFICIAL) {
            // Pause ExoPlayer and play via official embedded YouTube Player
            if (engine.player.isPlaying) engine.player.pause()
            _uiState.update { it.copy(activeYouTubeTrack = selected) }
        } else {
            _uiState.update {
                it.copy(
                    activeYouTubeTrack = null,
                    currentQueue = tracks,
                    currentTrack = selected,
                    durationMs = selected.durationMs.coerceAtLeast(1L)
                )
            }
            engine.playQueue(tracks, index)
        }
    }

    fun closeYouTubePlayer() {
        _uiState.update { it.copy(activeYouTubeTrack = null) }
    }

    fun openNowPlaying(open: Boolean) {
        _uiState.update { it.copy(showNowPlaying = open) }
    }

    fun togglePlayPause() = engine.togglePlayPause()
    fun skipNext() = engine.skipNext()
    fun skipPrevious() = engine.skipPrevious()
    fun seekTo(ms: Long) = engine.seekTo(ms)

    override fun onCleared() {
        engine.release()
        super.onCleared()
    }
}
