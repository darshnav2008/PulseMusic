package com.pulsemusic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.Icon
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
import com.pulsemusic.app.model.YouTubePlaylistItem
import com.pulsemusic.app.ui.components.pressureTouch
import com.pulsemusic.app.ui.theme.ApplePink
import com.pulsemusic.app.ui.theme.GlassSurface
import com.pulsemusic.app.ui.theme.TextPrimary
import com.pulsemusic.app.ui.theme.TextSecondary

@Composable
fun YouTubeConnectBanner(
    connectedEmail: String?,
    statusMessage: String?,
    playlists: List<YouTubePlaylistItem>,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onPlaylistClick: (YouTubePlaylistItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (connectedEmail != null) "YouTube Music Connected" else "Connect YouTube Music",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = connectedEmail
                        ?: "Sync your playlists & search YouTube Music via official API",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }

            Spacer(Modifier.width(12.dp))

            if (connectedEmail == null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(ApplePink)
                        .pressureTouch(onClick = onConnectClick)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Link,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Connect", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.1f))
                        .pressureTouch(onClick = onDisconnectClick)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF34C759),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Rounded.Logout,
                        contentDescription = "Disconnect",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (!statusMessage.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(text = statusMessage, color = ApplePink, fontSize = 12.sp)
        }

        if (playlists.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Your YouTube Playlists",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(playlists, key = { it.id }) { playlist ->
                    Column(
                        modifier = Modifier
                            .width(124.dp)
                            .pressureTouch(onClick = { onPlaylistClick(playlist) })
                    ) {
                        Box(
                            modifier = Modifier
                                .size(124.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF282832))
                        ) {
                            AsyncImage(
                                model = playlist.thumbnailUrl,
                                contentDescription = playlist.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = playlist.title,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${playlist.itemCount} tracks",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
