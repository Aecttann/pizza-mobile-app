package com.aectann.pizzamobileapp.ui.catalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aectann.pizzamobileapp.ui.catalog.CatalogTestTags
import com.aectann.pizzamobileapp.ui.theme.ColorAccent
import com.aectann.pizzamobileapp.ui.theme.ColorHighlight
import com.aectann.pizzamobileapp.ui.theme.ColorText
import com.aectann.pizzamobileapp.ui.theme.ColorWhite

private val PillShape = RoundedCornerShape(36.dp)

@Composable
fun OrderBar(
    quantity: Int,
    totalPrice: Double,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth < 360.dp) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    QuantityControl(
                        quantity = quantity,
                        onIncrement = onIncrement,
                        onDecrement = onDecrement,
                    )

                    PriceLabel(totalPrice = totalPrice)
                }

                AddButton(modifier = Modifier.fillMaxWidth())
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuantityControl(
                    quantity = quantity,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement,
                )

                PriceLabel(totalPrice = totalPrice)

                AddButton(modifier = Modifier.width(83.dp))
            }
        }
    }
}

@Composable
private fun PriceLabel(totalPrice: Double) {
    Text(
        text = "$" + formatPrice(totalPrice),
        style = MaterialTheme.typography.displaySmall,
        color = ColorText,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .width(100.dp)
            .testTag(CatalogTestTags.PRICE),
    )
}

@Composable
private fun AddButton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(PillShape)
            .background(ColorAccent)
            .testTag(CatalogTestTags.ADD),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Add",
            style = MaterialTheme.typography.displaySmall,
            color = ColorWhite,
        )
    }
}

@Composable
private fun QuantityControl(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .width(143.dp)
            .background(ColorHighlight, PillShape),
    ) {
        QuantityButton(
            text = "-",
            onClick = onDecrement,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .testTag(CatalogTestTags.DECREMENT),
        )

        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = ColorText,
            modifier = Modifier
                .align(Alignment.Center)
                .testTag(CatalogTestTags.QUANTITY),
        )

        QuantityButton(
            text = "+",
            onClick = onIncrement,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .testTag(CatalogTestTags.INCREMENT),
        )
    }
}

@Composable
private fun QuantityButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(elevation = 8.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(ColorWhite)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.displaySmall,
            color = ColorText,
            textAlign = TextAlign.Center,
        )
    }
}
