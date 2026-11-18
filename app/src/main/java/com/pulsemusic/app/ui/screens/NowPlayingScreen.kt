package com.pulsemusic.app.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pulsemusic.app.model.Track
import com.pulsemusic.app.ui.components.pressureTouch
import com.pulsemusic.app.ui.theme.ApplePink

/**
 * Full-screen Apple Music inspired Now Playing view:
 * - Dynamic atmospheric gradient background
 * - Artwork smoothly expands when playing and contracts when paused
 * - Tactile pressure-touch transport controls
 */
@Composable
fun NowPlayingScreen(
    track: Track,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val artworkScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.86f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "apple_music_artwork_scale"
    )

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(track.accentHex).copy(alpha = 0.55f),
            Color(0xFF1A1220),
            Color(0xFF0A0A0D)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top drag/dismiss handle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .pressureTouch(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Minimize",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        // Apple Music dynamic scaling artwork
        AsyncImage(
            model = track.artworkUrl,
            contentDescription = track.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = artworkScale
                    scaleY = artworkScale
                }
                .shadow(28.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF22222A))
        )

        Spacer(Modifier.height(32.dp))

        // Track title & artist
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = track.title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = track.artist,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(20.dp))

        // Scrubber
        val safeDuration = durationMs.coerceAtLeast(1L)
        Slider(
            value = (positionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f),
            onValueChange = { fraction -> onSeek((fraction * safeDuration).toLong()) },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(positionMs), color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Text(formatTime(durationMs), color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        }

        Spacer(Modifier.height(26.dp))

        // Pressure-touch transport controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .pressureTouch(pressedScale = 0.85f, onClick = onPrevious),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .pressureTouch(pressedScale = 0.88f, onClick = onTogglePlayPause),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play or Pause",
                    tint = Color.Black,
                    modifier = Modifier.size(44.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .pressureTouch(pressedScale = 0.85f, onClick = onNext),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "ZERO-LATENCY PRE-BUFFER ACTIVE",
            color = ApplePink.copy(alpha = 0.9f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
