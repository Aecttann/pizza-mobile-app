package com.aectann.pizzamobileapp.ui.catalog.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aectann.pizzamobileapp.data.model.PizzaSize
import com.aectann.pizzamobileapp.ui.catalog.CatalogTestTags
import com.aectann.pizzamobileapp.ui.theme.ColorActive
import com.aectann.pizzamobileapp.ui.theme.ColorText
import com.aectann.pizzamobileapp.ui.theme.ColorWhite

data class SizeSelectorCurveAnchors(
    val sideCenterX: Float,
    val sideCenterY: Float,
    val middleCenterX: Float,
    val middleCenterY: Float,
)

private val SelectorWidth = 244.dp
private val SelectorHeight = 64.dp
private val PillTouchSize = 52.dp
private val PillCircleSize = 48.dp
private val SidePillVerticalOffset = (-16).dp

@Composable
fun SizeSelector(
    selectedSize: PizzaSize,
    onSizeSelected: (PizzaSize) -> Unit,
    modifier: Modifier = Modifier,
    onCurveAnchorsMeasured: ((SizeSelectorCurveAnchors) -> Unit)? = null,
) {
    val density = LocalDensity.current
    val sideCenterXOffset = PillTouchSize / 2
    val middleCenterXOffset = SelectorWidth / 2
    val sideCenterOffset = SelectorHeight.centerOffset(SidePillVerticalOffset)
    val middleCenterOffset = SelectorHeight.centerOffset()

    Row(
        modifier = modifier
            .width(SelectorWidth)
            .height(SelectorHeight)
            .onGloballyPositioned { coordinates ->
                if (onCurveAnchorsMeasured != null) {
                    val selectorPosition = coordinates.positionInRoot()
                    onCurveAnchorsMeasured(
                        SizeSelectorCurveAnchors(
                            sideCenterX = selectorPosition.x + with(density) { sideCenterXOffset.toPx() },
                            sideCenterY = selectorPosition.y + with(density) { sideCenterOffset.toPx() },
                            middleCenterX = selectorPosition.x + with(density) { middleCenterXOffset.toPx() },
                            middleCenterY = selectorPosition.y + with(density) { middleCenterOffset.toPx() },
                        ),
                    )
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PizzaSize.entries.forEach { size ->
            SizePill(
                label = size.name,
                selected = size == selectedSize,
                onClick = { onSizeSelected(size) },
                tag = CatalogTestTags.sizePill(size),
            )
        }
    }
}

private fun Dp.centerOffset(verticalOffset: Dp = 0.dp): Dp = this / 2 + verticalOffset

@Composable
private fun SizePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String,
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) ColorActive else ColorWhite,
        animationSpec = spring(),
        label = "sizePillBg",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) ColorWhite else ColorText,
        animationSpec = spring(),
        label = "sizePillText",
    )

    Box(
        modifier = Modifier
            .offset(y = if (label == PizzaSize.M.name) 0.dp else SidePillVerticalOffset)
            .size(PillTouchSize)
            .clip(CircleShape)
            .background(if (selected) ColorWhite else Color.Transparent)
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .then(if (selected) Modifier else Modifier.shadow(elevation = 5.dp, shape = CircleShape))
                .size(PillCircleSize)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}
