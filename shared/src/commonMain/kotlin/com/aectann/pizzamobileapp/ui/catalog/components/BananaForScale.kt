package com.aectann.pizzamobileapp.ui.catalog.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import pizzamobileapp.shared.generated.resources.Res
import pizzamobileapp.shared.generated.resources.banana_scale
import pizzamobileapp.shared.generated.resources.banana_text

// Figma node 1411:441 "banana" - 97x63dp
// scale 1 (banana image) is rotated 180° in Figma and positioned at 17.46% top, 6.19% left
// Vector (curved text) covers top 44.44% of height, right-aligned with 7.22% right padding

@Composable
fun BananaForScale(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(97.dp)
            .height(63.dp),
    ) {
        // Banana image - Figma: rotate(180deg), positioned at inset(17.46% 0 0 6.19%)
        Image(
            painter = painterResource(Res.drawable.banana_scale),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = (97 * 0.0619f).dp,
                    top = (63 * 0.1746f).dp,
                )
                .graphicsLayer { rotationZ = 180f },
        )

        // Curved "Banana for scale" text - Figma: inset(0 7.22% 55.56% 0)
        Image(
            painter = painterResource(Res.drawable.banana_text),
            contentDescription = "Banana for scale",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width((97 * (1f - 0.0722f)).dp)
                .height((63 * (1f - 0.5556f)).dp),
        )
    }
}
