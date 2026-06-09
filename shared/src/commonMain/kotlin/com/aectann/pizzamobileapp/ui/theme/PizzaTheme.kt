package com.aectann.pizzamobileapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import pizzamobileapp.shared.generated.resources.Res
import pizzamobileapp.shared.generated.resources.Figtree_ExtraBold
import pizzamobileapp.shared.generated.resources.Figtree_Regular
import pizzamobileapp.shared.generated.resources.Figtree_SemiBold

// Design tokens
val ColorBackground = Color(0xFFFFFFFF)
val ColorHighlight = Color(0xFFF3E3DA)
val ColorAccent = Color(0xFF19C4EA)
val ColorActive = Color(0xFF000000)
val ColorText = Color(0xFF000000)
val ColorTextSecondary = Color(0xB3000000)
val ColorWhite = Color(0xFFFFFFFF)
val ColorShadow = Color(0x26000000)
val ColorNavScrim = Color(0x4D000000)

@Composable
fun figtreeFamily() = FontFamily(
    Font(Res.font.Figtree_Regular, weight = FontWeight.Normal),
    Font(Res.font.Figtree_SemiBold, weight = FontWeight.SemiBold),
    Font(Res.font.Figtree_ExtraBold, weight = FontWeight.ExtraBold),
)

@Composable
fun PizzaTheme(content: @Composable () -> Unit) {
    val figtree = figtreeFamily()
    val typography = Typography(
        headlineMedium = TextStyle(
            fontFamily = figtree,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.48).sp,
        ),
        titleSmall = TextStyle(
            fontFamily = figtree,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 20.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = figtree,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 24.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = figtree,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.36).sp,
        ),
        displaySmall = TextStyle(
            fontFamily = figtree,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            lineHeight = 24.sp,
        ),
    )
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = ColorBackground,
            surface = ColorBackground,
            primary = ColorAccent,
            onPrimary = ColorWhite,
            onBackground = ColorText,
            onSurface = ColorText,
        ),
        typography = typography,
        content = content,
    )
}
