package com.nexora.ai.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexora.ai.ui.theme.NexoraElectricCyan
import com.nexora.ai.ui.theme.NexoraPrimaryPurple
import com.nexora.ai.ui.theme.NexoraSurfaceVariant

/**
 * Animated glowing AI core with breathing pulsation.
 */
@Composable
fun PulsingAiCore(
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false,
    size: Dp = 100.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (isProcessing) 1.2f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isProcessing) 450 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .scale(scale)
    ) {
        // Outer glowing halo
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NexoraElectricCyan.copy(alpha = if (isProcessing) 0.5f else 0.25f),
                            NexoraPrimaryPurple.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
        // Middle ring
        Box(
            modifier = Modifier
                .size(size * 0.72f)
                .clip(CircleShape)
                .border(2.dp, NexoraElectricCyan.copy(alpha = 0.85f), CircleShape)
                .background(NexoraSurfaceVariant)
        )
        // Center Core
        Box(
            modifier = Modifier
                .size(size * 0.45f)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(NexoraPrimaryPurple, NexoraElectricCyan)
                    )
                )
        )
    }
}
