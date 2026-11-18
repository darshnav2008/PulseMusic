package com.pulsemusic.app.data

import com.pulsemusic.app.model.Track
import com.pulsemusic.app.model.TrackSource
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private data class AudiusTracksResponse(val data: List<AudiusTrackDto> = emptyList())

private data class AudiusTrackDto(
    val id: String,
    val title: String,
    val duration: Int = 0,
    val user: AudiusUserDto? = null,
    val artwork: Map<String, String>? = null
)

private data class AudiusUserDto(val name: String = "Independent Artist")

private interface AudiusApi {
    @GET("v1/tracks/trending")
    suspend fun getTrending(
        @Query("app_name") appName: String = "PulseMusic",
        @Query("limit") limit: Int = 25
    ): AudiusTracksResponse

    @GET("v1/tracks/search")
    suspend fun searchTracks(
        @Query("query") query: String,
        @Query("app_name") appName: String = "PulseMusic"
    ): AudiusTracksResponse
}

/**
 * Free, legal, ad-free music catalog via Audius Public API (no API key required).
 * Works out of the box so the app plays high-speed ad-free music immediately.
 */
class AudiusRepository {

    private val baseHost = "https://discoveryprovider.audius.co/"

    private val api: AudiusApi = Retrofit.Builder()
        .baseUrl(baseHost)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AudiusApi::class.java)

    suspend fun getTrendingTracks(): List<Track> = runCatching {
        api.getTrending().data.map { it.toDomain(baseHost) }
    }.getOrDefault(emptyList())

    suspend fun search(query: String): List<Track> = runCatching {
        if (query.isBlank()) return getTrendingTracks()
        api.searchTracks(query).data.map { it.toDomain(baseHost) }
    }.getOrDefault(emptyList())

    private fun AudiusTrackDto.toDomain(host: String): Track {
        val art = artwork?.get("480x480") ?: artwork?.get("150x150")
        return Track(
            id = "audius_$id",
            title = title,
            artist = user?.name ?: "Unknown Artist",
            album = "Audius Free",
            artworkUrl = art,
            streamUrl = "${host}v1/tracks/$id/stream?app_name=PulseMusic",
            durationMs = duration * 1000L,
            source = TrackSource.AUDIUS_FREE
        )
    }
}
