package com.app.todoleast.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    val swayAmplitude: Float,
    val swayFrequency: Float
)

@Composable
fun CelebrationEffect(
    show: Boolean,
    startPosition: Offset,
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
        Color(0xFF2EC4B6),
        Color(0xFF6C5CE7),
        Color(0xFFFF7675)
    )

    val particles = remember(startPosition) {
        List(60) {
            val angle = Random.nextFloat() * 360f
            val speed = 400f + Random.nextFloat() * 400f
            ConfettiParticle(
                velocityX = cos(Math.toRadians(angle.toDouble())).toFloat() * speed,
                velocityY = sin(Math.toRadians(angle.toDouble())).toFloat() * speed - 300f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                color = colors.random(),
                width = 8f + Random.nextFloat() * 8f,
                height = 12f + Random.nextFloat() * 12f,
                swayAmplitude = 10f + Random.nextFloat() * 20f,
                swayFrequency = 2f + Random.nextFloat() * 3f
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(show, startPosition) {
        if (show) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1800,
                    easing = LinearEasing
                )
            )
            onAnimationComplete()
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val time = progress.value
        val gravity = 800f

        particles.forEach { particle ->
            val t = time * 2f

            val sway = sin(t * particle.swayFrequency * Math.PI).toFloat() * particle.swayAmplitude

            val x = startPosition.x + particle.velocityX * t + sway
            val y = startPosition.y + particle.velocityY * t + 0.5f * gravity * t * t

            val currentRotation = particle.rotation + particle.rotationSpeed * t

            val alpha = when {
                time < 0.05f -> time * 20f
                time > 0.6f -> (1f - time) / 0.4f
                else -> 1f
            }.coerceIn(0f, 1f)

            if (y < size.height + 50 && y > -50 && x > -50 && x < size.width + 50 && alpha > 0) {
                rotate(
                    degrees = currentRotation,
                    pivot = Offset(x, y)
                ) {
                    drawRect(
                        color = particle.color.copy(alpha = alpha),
                        topLeft = Offset(x - particle.width / 2, y - particle.height / 2),
                        size = Size(particle.width, particle.height)
                    )
                }
            }
        }
    }
}
