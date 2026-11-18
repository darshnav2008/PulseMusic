package com.pulsemusic.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.pulsemusic.app.ui.screens.HomeScreen
import com.pulsemusic.app.ui.screens.NowPlayingScreen
import com.pulsemusic.app.ui.theme.PulseMusicTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PulseMusicTheme {
                val state by viewModel.uiState.collectAsState()

                // Request audio permission for local MP3/FLAC songs
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) viewModel.refreshLocalLibrary()
                }

                // Official Google Sign-In launcher for Connect YouTube Music
                val youtubeSignInLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    viewModel.onYouTubeSignInResult()
                }

                LaunchedEffect(Unit) {
                    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                    permissionLauncher.launch(audioPermission)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        state = state,
                        onTabSelected = viewModel::selectTab,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onSearchSubmit = viewModel::performSearch,
                        onTrackClick = viewModel::playTrack,
                        onConnectYouTube = {
                            youtubeSignInLauncher.launch(viewModel.getYouTubeSignInIntent())
                        },
                        onDisconnectYouTube = viewModel::disconnectYouTube,
                        onYouTubePlaylistClick = viewModel::openYouTubePlaylist,
                        onOpenNowPlaying = { viewModel.openNowPlaying(true) },
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onSkipNext = viewModel::skipNext,
                        onCloseYouTubePlayer = viewModel::closeYouTubePlayer
                    )

                    // Smooth Apple Music slide-up Now Playing sheet
                    val currentTrack = state.currentTrack
                    AnimatedVisibility(
                        visible = state.showNowPlaying && currentTrack != null,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        if (currentTrack != null) {
                            NowPlayingScreen(
                                track = currentTrack,
                                isPlaying = state.isPlaying,
                                positionMs = state.positionMs,
                                durationMs = state.durationMs,
                                onDismiss = { viewModel.openNowPlaying(false) },
                                onTogglePlayPause = viewModel::togglePlayPause,
                                onNext = viewModel::skipNext,
                                onPrevious = viewModel::skipPrevious,
                                onSeek = viewModel::seekTo
                            )
                        }
                    }
                }
            }
        }
    }
}
