package com.pulsemusic.app.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.pulsemusic.app.model.Track
import com.pulsemusic.app.model.TrackSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Scans MP3 / FLAC / M4A songs stored on the Android phone for true 0ms offline playback.
 */
class LocalMusicRepository(private val context: Context) {

    suspend fun loadLocalSongs(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        runCatching {
            context.contentResolver.query(collection, projection, selection, null, sortOrder)
                ?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        val contentUri = ContentUris.withAppendedId(collection, id)
                        val albumId = cursor.getLong(albumIdCol)
                        val artworkUri = ContentUris.withAppendedId(
                            android.net.Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        )

                        tracks += Track(
                            id = "local_$id",
                            title = cursor.getString(titleCol) ?: "Unknown Track",
                            artist = cursor.getString(artistCol) ?: "Unknown Artist",
                            album = cursor.getString(albumCol) ?: "Local Library",
                            artworkUrl = artworkUri.toString(),
                            streamUrl = contentUri.toString(),
                            durationMs = cursor.getLong(durationCol),
                            source = TrackSource.LOCAL_DEVICE
                        )
                    }
                }
        }
        tracks
    }
}
