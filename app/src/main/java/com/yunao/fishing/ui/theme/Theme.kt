package com.yunao.fishing.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val DeepSea = Color(0xFF0B5D7C)
val ReedGreen = Color(0xFF3C8A5B)
val SunsetAmber = Color(0xFFE0A22A)
val MistGray = Color(0xFFF3F6F7)
val DeepSeaDark = Color(0xFF0A3A4E)

private val LightColors = lightColorScheme(
    primary = DeepSea,
    secondary = ReedGreen,
    tertiary = SunsetAmber,
    background = MistGray,
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FC3E8),
    secondary = Color(0xFF8FD6AA),
    tertiary = SunsetAmber,
    background = Color(0xFF0E1B20),
    surface = Color(0xFF16262C),
)

@Composable
fun YuNaoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
