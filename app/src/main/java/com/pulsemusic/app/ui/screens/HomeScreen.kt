package com.pulsemusic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pulsemusic.app.LibraryTab
import com.pulsemusic.app.MainUiState
import com.pulsemusic.app.model.Track
import com.pulsemusic.app.model.YouTubePlaylistItem
import com.pulsemusic.app.ui.components.OfficialYouTubePlayerCard
import com.pulsemusic.app.ui.components.pressureTouch
import com.pulsemusic.app.ui.theme.ApplePink
import com.pulsemusic.app.ui.theme.DeepBackground
import com.pulsemusic.app.ui.theme.GlassSurface
import com.pulsemusic.app.ui.theme.TextPrimary
import com.pulsemusic.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    state: MainUiState,
    onTabSelected: (LibraryTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onTrackClick: (List<Track>, Int) -> Unit,
    onConnectYouTube: () -> Unit,
    onDisconnectYouTube: () -> Unit,
    onYouTubePlaylistClick: (YouTubePlaylistItem) -> Unit,
    onOpenNowPlaying: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onCloseYouTubePlayer: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Listen Now",
                    color = TextPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Source tabs
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LibraryTab.entries.forEach { tab ->
                        val selected = state.selectedTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) ApplePink else GlassSurface)
                                .pressureTouch(onClick = { onTabSelected(tab) })
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = tab.label,
                                color = if (selected) Color.White else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = when (state.selectedTab) {
                                LibraryTab.YOUTUBE_MUSIC -> "Search YouTube Music..."
                                LibraryTab.LOCAL -> "Filter device songs..."
                                LibraryTab.FREE_AD_FREE -> "Search ad-free tracks..."
                            },
                            color = TextSecondary
                        )
                    },
                    singleLine = true,
                    trailingIcon = {
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clip(CircleShape)
                                .background(ApplePink)
                                .pressureTouch(onClick = onSearchSubmit)
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GlassSurface,
                        unfocusedContainerColor = GlassSurface,
                        focusedBorderColor = ApplePink,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // YouTube Connect Card when on YouTube Music tab
            if (state.selectedTab == LibraryTab.YOUTUBE_MUSIC) {
                item {
                    YouTubeConnectBanner(
                        connectedEmail = state.youtubeAccountEmail,
                        statusMessage = state.youtubeStatusMessage,
                        playlists = state.youtubePlaylists,
                        onConnectClick = onConnectYouTube,
                        onDisconnectClick = onDisconnectYouTube,
                        onPlaylistClick = onYouTubePlaylistClick
                    )
                }
            }

            // Official embedded YouTube Player when playing a YouTube track
            if (state.activeYouTubeTrack != null) {
                item {
                    OfficialYouTubePlayerCard(
                        track = state.activeYouTubeTrack,
                        onClose = onCloseYouTubePlayer
                    )
                }
            }

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ApplePink)
                    }
                }
            } else {
                val activeList = state.visibleTracks
                if (activeList.isEmpty()) {
                    item {
                        Text(
                            text = when (state.selectedTab) {
                                LibraryTab.LOCAL -> "No local songs found yet. Grant audio permission or add MP3/FLAC files to your phone."
                                LibraryTab.YOUTUBE_MUSIC -> "Connect your YouTube account above or search to browse tracks."
                                LibraryTab.FREE_AD_FREE -> "No tracks loaded. Check internet connection and tap Search."
                            },
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                } else {
                    itemsIndexed(activeList, key = { _, t -> t.id }) { index, track ->
                        TrackRow(
                            track = track,
                            isActive = state.currentTrack?.id == track.id,
                            onClick = { onTrackClick(activeList, index) }
                        )
                    }
                }
            }
        }

        // Apple Music floating Mini-Player bar
        val playingTrack = state.currentTrack
        if (playingTrack != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xF024242C))
                    .pressureTouch(pressedScale = 0.97f, onClick = onOpenNowPlaying)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = playingTrack.artworkUrl,
                    contentDescription = playingTrack.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF33333E))
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playingTrack.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = playingTrack.artist,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .pressureTouch(pressedScale = 0.85f, onClick = onTogglePlayPause),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .pressureTouch(pressedScale = 0.85f, onClick = onSkipNext),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackRow(
    track: Track,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) ApplePink.copy(alpha = 0.16f) else GlassSurface)
            .pressureTouch(pressedScale = 0.96f, onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2B2B36)),
            contentAlignment = Alignment.Center
        ) {
            if (!track.artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = TextSecondary)
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isActive) ApplePink else TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = track.artist,
                color = TextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
