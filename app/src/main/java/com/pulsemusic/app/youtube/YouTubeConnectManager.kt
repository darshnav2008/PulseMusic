package com.pulsemusic.app.youtube

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.pulsemusic.app.BuildConfig
import com.pulsemusic.app.model.Track
import com.pulsemusic.app.model.TrackSource
import com.pulsemusic.app.model.YouTubePlaylistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handles official Google OAuth sign-in for connecting the user's YouTube / YouTube Music
 * library and playlists using YouTube Data API v3 (no YouTube Music app needed on device).
 */
class YouTubeConnectManager(private val context: Context) {

    companion object {
        private const val YOUTUBE_READONLY_SCOPE = "https://www.googleapis.com/auth/youtube.readonly"
    }

    private val api = YouTubeApiService.create()

    private val signInOptions: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(YOUTUBE_READONLY_SCOPE))
            .build()
    }

    private val signInClient by lazy {
        GoogleSignIn.getClient(context, signInOptions)
    }

    fun getSignInIntent(): Intent = signInClient.signInIntent

    fun getSignedInAccount(): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

    fun signOut(onComplete: () -> Unit) {
        signInClient.signOut().addOnCompleteListener { onComplete() }
    }

    private suspend fun getAccessToken(account: GoogleSignInAccount): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                account.account?.let {
                    GoogleAuthUtil.getToken(context, it, "oauth2:$YOUTUBE_READONLY_SCOPE")
                }
            }.getOrNull()
        }

    suspend fun fetchMyPlaylists(): Result<List<YouTubePlaylistItem>> = withContext(Dispatchers.IO) {
        val account = getSignedInAccount()
            ?: return@withContext Result.failure(IllegalStateException("Not connected to YouTube account"))
        val token = getAccessToken(account)
            ?: return@withContext Result.failure(IllegalStateException("Could not get OAuth token. Check Google Cloud setup."))

        runCatching {
            api.getMyPlaylists("Bearer $token").items.map { dto ->
                YouTubePlaylistItem(
                    id = dto.id,
                    title = dto.snippet.title,
                    itemCount = dto.contentDetails?.itemCount ?: 0,
                    thumbnailUrl = dto.snippet.thumbnails?.bestUrl()
                )
            }
        }
    }

    suspend fun fetchPlaylistTracks(playlistId: String): Result<List<Track>> =
        withContext(Dispatchers.IO) {
            val account = getSignedInAccount()
                ?: return@withContext Result.failure(IllegalStateException("Not connected"))
            val token = getAccessToken(account)
                ?: return@withContext Result.failure(IllegalStateException("Missing OAuth token"))

            runCatching {
                api.getPlaylistTracks("Bearer $token", playlistId = playlistId).items.mapNotNull { item ->
                    val videoId = item.snippet.resourceId?.videoId ?: return@mapNotNull null
                    Track(
                        id = "yt_$videoId",
                        title = item.snippet.title,
                        artist = item.snippet.videoOwnerChannelTitle ?: "YouTube Music",
                        album = "YouTube Playlist",
                        artworkUrl = item.snippet.thumbnails?.bestUrl(),
                        youtubeVideoId = videoId,
                        source = TrackSource.YOUTUBE_OFFICIAL
                    )
                }
            }
        }

    suspend fun searchYouTubeMusic(query: String): Result<List<Track>> =
        withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.YOUTUBE_API_KEY
            if (apiKey.startsWith("YOUR_")) {
                return@withContext Result.failure(
                    IllegalStateException("Add your free YouTube Data API v3 Key in app/build.gradle.kts first.")
                )
            }
            runCatching {
                api.searchMusic(apiKey = apiKey, query = query).items.mapNotNull { item ->
                    val videoId = item.id.videoId ?: return@mapNotNull null
                    Track(
                        id = "yt_$videoId",
                        title = item.snippet.title,
                        artist = item.snippet.channelTitle ?: "YouTube Music",
                        album = "YouTube Music",
                        artworkUrl = item.snippet.thumbnails?.bestUrl(),
                        youtubeVideoId = videoId,
                        source = TrackSource.YOUTUBE_OFFICIAL
                    )
                }
            }
        }
}
