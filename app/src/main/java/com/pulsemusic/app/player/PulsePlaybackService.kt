package com.pulsemusic.app.player

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Background playback service so music continues smoothly when screen is off,
 * with lock-screen & notification media controls.
 */
class PulsePlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var engine: ZeroLatencyAudioEngine? = null

    override fun onCreate() {
        super.onCreate()
        val audioEngine = ZeroLatencyAudioEngine(this)
        engine = audioEngine
        mediaSession = MediaSession.Builder(this, audioEngine.player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.release()
        engine?.release()
        mediaSession = null
        engine = null
        super.onDestroy()
    }
}
