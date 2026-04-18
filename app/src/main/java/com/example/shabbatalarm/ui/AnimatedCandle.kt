package com.example.shabbatalarm.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap

/**
 * A Shabbat candle drawn entirely in Compose Canvas.
 * When [isLit] is true the flame flickers and sways gently.
 */
@Composable
fun AnimatedCandle(
    isLit: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "candle")
    val flicker by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flicker"
    )
    val sway by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    val candleColor = MaterialTheme.colorScheme.surfaceVariant
    val candleShadow = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
    val wickColor = MaterialTheme.colorScheme.onSurface
    val flameOuter = Color(0xFFFFC107)
    val flameInner = Color(0xFFFF6F00)
    val flameTip = Color(0xFFFFF59D)
    val glowColor = Color(0xFFFFD54F)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        // Candle body (rounded rectangle)
        val bodyWidth = w * 0.28f
        val bodyHeight = h * 0.45f
        val bodyTop = h * 0.55f
        drawRoundRect(
            color = candleColor,
            topLeft = Offset(cx - bodyWidth / 2f, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(bodyWidth * 0.12f)
        )
        // Right-side shadow for depth
        drawRoundRect(
            color = candleShadow,
            topLeft = Offset(cx + bodyWidth / 2f - bodyWidth * 0.22f, bodyTop),
            size = Size(bodyWidth * 0.22f, bodyHeight),
            cornerRadius = CornerRadius(bodyWidth * 0.1f)
        )

        // Wick
        val wickTopY = bodyTop - h * 0.05f
        drawLine(
            color = wickColor,
            start = Offset(cx, wickTopY),
            end = Offset(cx, bodyTop),
            strokeWidth = w * 0.02f,
            cap = StrokeCap.Round
        )

        if (isLit) {
            val flameCenterX = cx + sway
            val flameBaseY = bodyTop - h * 0.02f
            val flameTopY = wickTopY - h * 0.28f * flicker
            val flameWidth = w * 0.18f * flicker
            val glowCenter = Offset(flameCenterX, (flameBaseY + flameTopY) / 2f)

            // Glow halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.35f),
                        glowColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = glowCenter,
                    radius = w * 0.5f
                ),
                center = glowCenter,
                radius = w * 0.5f
            )

            // Outer flame (amber teardrop)
            val outerFlame = Path().apply {
                moveTo(flameCenterX, flameTopY)
                cubicTo(
                    flameCenterX - flameWidth, flameTopY + h * 0.08f,
                    flameCenterX - flameWidth * 0.8f, flameBaseY - h * 0.04f,
                    flameCenterX, flameBaseY
                )
                cubicTo(
                    flameCenterX + flameWidth * 0.8f, flameBaseY - h * 0.04f,
                    flameCenterX + flameWidth, flameTopY + h * 0.08f,
                    flameCenterX, flameTopY
                )
                close()
            }
            drawPath(path = outerFlame, color = flameOuter)

            // Inner flame (deep orange)
            val innerFlame = Path().apply {
                moveTo(flameCenterX, flameTopY + h * 0.08f)
                cubicTo(
                    flameCenterX - flameWidth * 0.55f, flameTopY + h * 0.14f,
                    flameCenterX - flameWidth * 0.45f, flameBaseY - h * 0.06f,
                    flameCenterX, flameBaseY - h * 0.06f
                )
                cubicTo(
                    flameCenterX + flameWidth * 0.45f, flameBaseY - h * 0.06f,
                    flameCenterX + flameWidth * 0.55f, flameTopY + h * 0.14f,
                    flameCenterX, flameTopY + h * 0.08f
                )
                close()
            }
            drawPath(path = innerFlame, color = flameInner)

            // Bright tip
            drawCircle(
                color = flameTip,
                radius = w * 0.025f,
                center = Offset(flameCenterX, flameTopY + h * 0.11f)
            )
        }
    }
}
