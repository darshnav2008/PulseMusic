package com.pulsemusic.app.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.pulsemusic.app.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Ultra-low-latency audio engine built on AndroidX Media3 (ExoPlayer).
 * - Uses persistent HTTP/2 connections via OkHttp
 * - 250 MB LRU disk cache for instant replay & pre-buffered switching
 * - 250 ms playback start threshold
 * - Pre-caches the next track in the queue so "Next" starts with near-zero delay
 */
@OptIn(UnstableApi::class)
class ZeroLatencyAudioEngine(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var preloadJob: Job? = null

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val simpleCache: SimpleCache by lazy {
        val cacheDir = File(appContext.cacheDir, "pulse_audio_cache")
        val evictor = LeastRecentlyUsedCacheEvictor(250L * 1024L * 1024L) // 250 MB
        SimpleCache(cacheDir, evictor, StandaloneDatabaseProvider(appContext))
    }

    private val cacheDataSourceFactory: CacheDataSource.Factory by lazy {
        val httpFactory = OkHttpDataSource.Factory(okHttpClient)
        val upstreamFactory = DefaultDataSource.Factory(appContext, httpFactory)
        CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    val player: ExoPlayer = run {
        // Tuned LoadControl for instant start (250ms) and smooth gapless playback
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                15_000, // minBufferMs
                50_000, // maxBufferMs
                250,    // bufferForPlaybackMs (instant start!)
                1_000   // bufferForPlaybackAfterRebufferMs
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        ExoPlayer.Builder(appContext)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    private var currentQueue: List<Track> = emptyList()

    fun playQueue(tracks: List<Track>, startIndex: Int) {
        val playable = tracks.filter { !it.streamUrl.isNullOrBlank() }
        if (playable.isEmpty()) return

        currentQueue = playable
        val safeIndex = startIndex.coerceIn(0, playable.lastIndex)

        val mediaItems = playable.map { track ->
            MediaItem.Builder()
                .setMediaId(track.id)
                .setUri(track.streamUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setArtworkUri(track.artworkUrl?.let(Uri::parse))
                        .build()
                )
                .build()
        }

        player.setMediaItems(mediaItems, safeIndex, 0L)
        player.prepare()
        player.play()

        // Immediately pre-buffer the upcoming track for zero-latency switching
        preloadNextTrack(safeIndex)
    }

    fun preloadNextTrack(currentIndex: Int) {
        val nextTrack = currentQueue.getOrNull(currentIndex + 1) ?: return
        val nextUrl = nextTrack.streamUrl ?: return
        if (!nextUrl.startsWith("http")) return // Local files are already instant

        preloadJob?.cancel()
        preloadJob = scope.launch {
            runCatching {
                // Pre-cache the first 1.5 MB of the next track (~40-60 seconds of audio)
                val dataSpec = DataSpec.Builder()
                    .setUri(Uri.parse(nextUrl))
                    .setPosition(0)
                    .setLength(1_500_000L)
                    .build()
                val cacheDataSource = cacheDataSourceFactory.createDataSource()
                CacheWriter(cacheDataSource, dataSpec, null, null).cache()
            }
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun skipNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            preloadNextTrack(player.currentMediaItemIndex)
        }
    }

    fun skipPrevious() {
        if (player.currentPosition > 3_000L) {
            player.seekTo(0)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    fun release() {
        preloadJob?.cancel()
        player.release()
        simpleCache.release()
    }
}
