package com.app.todoleast.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Confetti(
    val x: Float,
    val y: Float,
    val angle: Float,
    val speed: Float,
    val color: Color,
    val size: Float
)

@Composable
fun CelebrationEffect(
    show: Boolean,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!show) return

    val colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFFFFE66D),
        Color(0xFF95E1D3),
        Color(0xFFF38181),
        Color(0xFFAA96DA),
        Color(0xFFFCBF49),
        Color(0xFF2EC4B6)
    )

    val confettiCount = 100
    val confettis = remember {
        List(confettiCount) {
            Confetti(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.3f,
                angle = Random.nextFloat() * 360f,
                speed = 0.5f + Random.nextFloat() * 1.5f,
                color = colors.random(),
                size = 8f + Random.nextFloat() * 12f
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(show) {
        if (show) {
            launch {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 2000,
                        easing = FastOutSlowInEasing
                    )
                )
                onAnimationComplete()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val currentProgress = progress.value

            confettis.forEach { confetti ->
                val gravity = 2f
                val time = currentProgress * confetti.speed

                val dx = cos(Math.toRadians(confetti.angle.toDouble())).toFloat() * 200f * time
                val dy = sin(Math.toRadians(confetti.angle.toDouble())).toFloat() * 100f * time +
                        gravity * time * time * 500f

                val x = confetti.x * size.width + dx
                val y = confetti.y * size.height + dy

                val alpha = (1f - currentProgress).coerceIn(0f, 1f)
                val rotation = confetti.angle + currentProgress * 720f

                if (y < size.height && alpha > 0) {
                    drawCircle(
                        color = confetti.color.copy(alpha = alpha),
                        radius = confetti.size * (1f - currentProgress * 0.3f),
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}
