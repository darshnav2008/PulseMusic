package com.pulsemusic.app.youtube

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class YtPlaylistListResponse(val items: List<YtPlaylistDto> = emptyList())

data class YtPlaylistDto(
    val id: String,
    val snippet: YtSnippetDto,
    val contentDetails: YtContentDetailsDto? = null
)

data class YtContentDetailsDto(val itemCount: Int = 0)

data class YtPlaylistItemsResponse(val items: List<YtPlaylistTrackDto> = emptyList())

data class YtPlaylistTrackDto(val snippet: YtPlaylistItemSnippetDto)

data class YtPlaylistItemSnippetDto(
    val title: String,
    val videoOwnerChannelTitle: String? = null,
    val thumbnails: YtThumbnailsDto? = null,
    val resourceId: YtResourceIdDto? = null
)

data class YtResourceIdDto(val videoId: String? = null)

data class YtSearchResponse(val items: List<YtSearchItemDto> = emptyList())

data class YtSearchItemDto(
    val id: YtSearchIdDto,
    val snippet: YtSnippetDto
)

data class YtSearchIdDto(val videoId: String? = null)

data class YtSnippetDto(
    val title: String,
    val channelTitle: String? = null,
    val thumbnails: YtThumbnailsDto? = null
)

data class YtThumbnailsDto(
    val high: YtThumbUrlDto? = null,
    val medium: YtThumbUrlDto? = null,
    val default: YtThumbUrlDto? = null
) {
    fun bestUrl(): String? = high?.url ?: medium?.url ?: default?.url
}

data class YtThumbUrlDto(val url: String)

/**
 * Official YouTube Data API v3 client for fetching user playlists and searching music.
 */
interface YouTubeApiService {

    @GET("youtube/v3/playlists")
    suspend fun getMyPlaylists(
        @Header("Authorization") bearerToken: String,
        @Query("part") part: String = "snippet,contentDetails",
        @Query("mine") mine: Boolean = true,
        @Query("maxResults") maxResults: Int = 25
    ): YtPlaylistListResponse

    @GET("youtube/v3/playlistItems")
    suspend fun getPlaylistTracks(
        @Header("Authorization") bearerToken: String,
        @Query("part") part: String = "snippet",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 50
    ): YtPlaylistItemsResponse

    @GET("youtube/v3/search")
    suspend fun searchMusic(
        @Query("key") apiKey: String,
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("videoCategoryId") categoryId: String = "10", // Music category
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 20
    ): YtSearchResponse

    companion object {
        fun create(): YouTubeApiService = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YouTubeApiService::class.java)
    }
}
