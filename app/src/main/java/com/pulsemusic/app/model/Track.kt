package com.pulsemusic.app.model

enum class TrackSource {
    AUDIUS_FREE,
    LOCAL_DEVICE,
    YOUTUBE_OFFICIAL
}

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "Single",
    val artworkUrl: String? = null,
    val streamUrl: String? = null,      // Direct audio URL for ExoPlayer (Audius / Local)
    val youtubeVideoId: String? = null, // Video ID for official YouTube Player
    val durationMs: Long = 0L,
    val source: TrackSource = TrackSource.AUDIUS_FREE,
    val accentHex: Long = 0xFFFA2D48    // Apple Music signature pink-red fallback
)

data class YouTubePlaylistItem(
    val id: String,
    val title: String,
    val itemCount: Int,
    val thumbnailUrl: String?
)
