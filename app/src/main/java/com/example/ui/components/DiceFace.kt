package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.DiceTheme

@Composable
fun DiceFace(
    value: Int,
    theme: DiceTheme,
    isRolling: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    // Rolling Animation - Rotation & Wobble
    val infiniteTransition = rememberInfiniteTransition(label = "dice_roll_transition")
    val rotationZ by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation_z"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_pulse"
    )

    val rotationYAnim = remember { Animatable(0f) }
    LaunchedEffect(isRolling) {
        if (isRolling) {
            rotationYAnim.animateTo(
                targetValue = 720f,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
            rotationYAnim.snapTo(0f)
        }
    }

    val animatedRotationZ = if (isRolling) rotationZ else 0f
    val animatedScale = if (isRolling) scalePulse else 1f

    Box(
        modifier = modifier
            .size(size)
            .aspectRatio(1f)
            .graphicsLayer {
                this.rotationZ = animatedRotationZ
                this.rotationY = rotationYAnim.value
                this.scaleX = animatedScale
                this.scaleY = animatedScale
                this.cameraDistance = 12 * density
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val cornerRadius = canvasWidth * 0.20f
            val pipRadius = canvasWidth * 0.085f

            // 1. Draw Outer Subtle Shadow / Glow
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.35f),
                topLeft = Offset(canvasWidth * 0.04f, canvasHeight * 0.06f),
                size = Size(canvasWidth * 0.92f, canvasHeight * 0.92f),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )

            // 2. Draw Die Body Surface with Gradient
            val bgGradient = Brush.linearGradient(
                colors = listOf(
                    theme.backgroundColor,
                    theme.backgroundColor.copy(
                        red = (theme.backgroundColor.red * 0.85f).coerceIn(0f, 1f),
                        green = (theme.backgroundColor.green * 0.85f).coerceIn(0f, 1f),
                        blue = (theme.backgroundColor.blue * 0.85f).coerceIn(0f, 1f)
                    )
                ),
                start = Offset(0f, 0f),
                end = Offset(canvasWidth, canvasHeight)
            )

            drawRoundRect(
                brush = bgGradient,
                topLeft = Offset(0f, 0f),
                size = Size(canvasWidth, canvasHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )

            // 3. Draw Die Border
            drawRoundRect(
                color = theme.borderColor,
                topLeft = Offset(0f, 0f),
                size = Size(canvasWidth, canvasHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = canvasWidth * 0.025f)
            )

            // 4. Draw Bevel Top Highlight
            drawRoundRect(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(canvasWidth * 0.03f, canvasHeight * 0.03f),
                size = Size(canvasWidth * 0.94f, canvasHeight * 0.94f),
                cornerRadius = CornerRadius(cornerRadius * 0.85f, cornerRadius * 0.85f),
                style = Stroke(width = canvasWidth * 0.015f)
            )

            // 5. Compute Pip Coordinates
            val left = canvasWidth * 0.28f
            val centerX = canvasWidth * 0.50f
            val right = canvasWidth * 0.72f

            val top = canvasHeight * 0.28f
            val centerY = canvasHeight * 0.50f
            val bottom = canvasHeight * 0.72f

            fun drawPip(x: Float, y: Float, isCenter: Boolean = false) {
                val color = if (isCenter) theme.centerPipColor else theme.pipColor
                // Draw slight inset shadow inside pip
                drawCircle(
                    color = Color.Black.copy(alpha = 0.2f),
                    radius = pipRadius * 1.1f,
                    center = Offset(x + canvasWidth * 0.01f, y + canvasHeight * 0.01f)
                )
                // Draw Pip
                drawCircle(
                    color = color,
                    radius = pipRadius,
                    center = Offset(x, y)
                )
                // Draw subtle pip shine highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = pipRadius * 0.35f,
                    center = Offset(x - pipRadius * 0.3f, y - pipRadius * 0.3f)
                )
            }

            // Draw Pips based on Value
            val safeValue = value.coerceIn(1, 6)
            when (safeValue) {
                1 -> {
                    drawPip(centerX, centerY, isCenter = true)
                }
                2 -> {
                    drawPip(left, top)
                    drawPip(right, bottom)
                }
                3 -> {
                    drawPip(left, top)
                    drawPip(centerX, centerY, isCenter = true)
                    drawPip(right, bottom)
                }
                4 -> {
                    drawPip(left, top)
                    drawPip(right, top)
                    drawPip(left, bottom)
                    drawPip(right, bottom)
                }
                5 -> {
                    drawPip(left, top)
                    drawPip(right, top)
                    drawPip(centerX, centerY, isCenter = true)
                    drawPip(left, bottom)
                    drawPip(right, bottom)
                }
                6 -> {
                    drawPip(left, top)
                    drawPip(left, centerY)
                    drawPip(left, bottom)
                    drawPip(right, top)
                    drawPip(right, centerY)
                    drawPip(right, bottom)
                }
            }
        }
    }
}
