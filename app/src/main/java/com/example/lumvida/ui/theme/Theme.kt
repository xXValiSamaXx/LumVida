/*define un tema personalizado para la aplicación LumVida utilizando Jetpack Compose.
 En primer lugar, se establecen dos esquemas de colores: DarkColorScheme y LightColorScheme,
  que determinan los colores primarios, secundarios, de fondo, de superficie y de texto según
   el modo de tema (oscuro o claro). Luego, en la función LumVivaTheme, se selecciona el esquema
    de colores adecuado según si el sistema está en modo oscuro o claro, y si el dispositivo es
    compatible con colores dinámicos (disponibles desde Android 12).*/


package com.example.lumvida.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = PrimaryDark,
    surface = PrimaryDark,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = TextPrimary,
    surface = TextPrimary,
    onPrimary = TextPrimary,
    onSecondary = PrimaryDark,
    onBackground = PrimaryDark,
    onSurface = PrimaryDark
)

@Composable
fun LumVivaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}