package com.jianwang.vocab9000.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BrandIndigo = Color(0xFF4F46E5)
val LearnedGreen = Color(0xFF16A34A)
val AgainOrange = Color(0xFFEA580C)
val HardYellow = Color(0xFFCA8A04)
val EasyBlue = Color(0xFF2563EB)

private val LightColors = lightColorScheme(
    primary = BrandIndigo,
    secondary = Color(0xFF6366F1),
    tertiary = LearnedGreen,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF818CF8),
    secondary = Color(0xFFA5B4FC),
    tertiary = Color(0xFF4ADE80),
)

@Composable
fun Vocab9000Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
