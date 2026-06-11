package com.aectann.pizzamobileapp.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.aectann.pizzamobileapp.ui.theme.ColorWhite
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import pizzamobileapp.shared.generated.resources.Res
import pizzamobileapp.shared.generated.resources.splash_pizza

private const val SLICE_COUNT = 8
private const val SLICE_STAGGER_MS = 24L
private const val SLICE_ANIM_MS = 18
private val PIZZA_SIZE = 270.dp
private const val SLICE_INITIAL_SCALE = 0.96f

private data class SliceAngles(
    val start: Float,
    val sweep: Float,
)

private val SliceMasks = listOf(
    SliceAngles(start = -87f, sweep = 52f),
    SliceAngles(start = -35f, sweep = 39f),
    SliceAngles(start = 4f, sweep = 38f),
    SliceAngles(start = 42f, sweep = 47f),
    SliceAngles(start = 89f, sweep = 42f),
    SliceAngles(start = 131f, sweep = 43f),
    SliceAngles(start = 174f, sweep = 47f),
    SliceAngles(start = -139f, sweep = 52f),
)

private val SliceRevealOrder = listOf(2, 3, 4, 5, 6, 7, 0, 1)

/**
 * Plays the pizza-assembly animation immediately on first composition.
 * The pizza is a local mixed-slice image (different topping per wedge), so the
 * assembly renders instantly without waiting on the network.
 * [onAnimationFinished] fires after animation + 300ms hold; caller decides when to navigate.
 */
@Composable
fun SplashScreen(
    onAnimationFinished: () -> Unit,
) {
    val painter = painterResource(Res.drawable.splash_pizza)
    val sliceAnims = remember { List(SLICE_COUNT) { Animatable(0f) } }

    // Start animation immediately - no waiting for image load.
    LaunchedEffect(Unit) {
        SliceRevealOrder.forEach { sliceIndex ->
            sliceAnims[sliceIndex].animateTo(
                targetValue = 1f,
                animationSpec = tween(SLICE_ANIM_MS, easing = FastOutSlowInEasing),
            )
            delay(SLICE_STAGGER_MS)
        }
        delay(50L)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorWhite),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.size(PIZZA_SIZE)) {
            repeat(SLICE_COUNT) { i ->
                val progress = sliceAnims[i].value
                val mask = SliceMasks[i]
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(PIZZA_SIZE)
                        .graphicsLayer {
                            alpha = progress
                            val scale = SLICE_INITIAL_SCALE + (1f - SLICE_INITIAL_SCALE) * progress
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(
                            WedgeShape(
                                startAngle = mask.start,
                                sweep = mask.sweep,
                            ),
                        ),
                )
            }
        }
    }
}

private class WedgeShape(
    private val startAngle: Float,
    private val sweep: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = minOf(size.width, size.height) / 2f
        val path = Path().apply {
            moveTo(cx, cy)
            arcTo(
                rect = Rect(Offset(cx - r, cy - r), Offset(cx + r, cy + r)),
                startAngleDegrees = startAngle,
                sweepAngleDegrees = sweep,
                forceMoveTo = false,
            )
            close()
        }
        return Outline.Generic(path)
    }
}
