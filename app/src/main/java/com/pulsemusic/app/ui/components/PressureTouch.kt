package com.pulsemusic.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Replicates an Apple-style pressure-touch tactile feel on Android:
 * - Immediate spring scale-down on touch
 * - Crisp haptic click on press-in
 * - Heavy haptic feedback on long-press (peek/options)
 */
fun Modifier.pressureTouch(
    pressedScale: Float = 0.94f,
    onLongPress: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pressure_touch_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(onClick, onLongPress) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    tryAwaitRelease()
                    isPressed = false
                },
                onLongPress = {
                    if (onLongPress != null) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    }
                },
                onTap = { onClick() }
            )
        }
}
