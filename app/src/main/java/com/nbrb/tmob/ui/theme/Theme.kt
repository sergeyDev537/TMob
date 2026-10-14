package com.nbrb.tmob.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Main,
    onPrimary = onMain,
    secondary = Charts,
    onSecondary = onMain,
    background = Background,
    onBackground = OnBackground,
    surface = Background,
    onSurface = OnBackground
)

@Composable
fun TMobTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
