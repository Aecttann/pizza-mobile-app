package com.aectann.pizzamobileapp.ui.catalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aectann.pizzamobileapp.ui.theme.ColorText
import com.aectann.pizzamobileapp.ui.theme.ColorTextSecondary
import com.aectann.pizzamobileapp.ui.theme.ColorWhite

@Composable
fun PizzaNavbar(
    pizzaName: String,
    modifier: Modifier = Modifier,
    enterProgress: Float = 1f,
) {
    // Transparent background so the peach ellipse shows behind the status bar and
    // the back/favourite buttons, matching the design.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(84.dp),
    ) {
        // Back button (static, no action required)
        NavCircleButton(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
                .graphicsLayer {
                    alpha = enterProgress
                    translationX = (enterProgress - 1f) * 32.dp.toPx()
                },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = ColorText,
                modifier = Modifier.size(24.dp),
            )
        }

        // Title centered
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    alpha = enterProgress
                    translationY = (enterProgress - 1f) * 20.dp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Pizzas",
                style = MaterialTheme.typography.titleSmall,
                color = ColorTextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = pizzaName,
                style = MaterialTheme.typography.headlineMedium,
                color = ColorText,
                textAlign = TextAlign.Center,
            )
        }

        // Favorite button (static, no action required)
        NavCircleButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
                .graphicsLayer {
                    alpha = enterProgress
                    translationX = (1f - enterProgress) * 32.dp.toPx()
                },
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Favourite",
                tint = ColorText,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun NavCircleButton(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(elevation = 5.dp, shape = CircleShape)
            .background(ColorWhite, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
