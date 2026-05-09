package br.com.fiap.gabinova.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary              = GabBlue,
    onPrimary            = GabOnPrimary,
    primaryContainer     = GabPrimaryContainer,
    onPrimaryContainer   = GabOnPrimaryContainer,

    secondary            = GabYellow,
    onSecondary          = GabOnSecondary,
    secondaryContainer   = GabSecondaryContainer,
    onSecondaryContainer = GabOnSecondaryContainer,

    tertiary             = GabGreen,
    onTertiary           = GabOnTertiary,
    tertiaryContainer    = GabTertiaryContainer,
    onTertiaryContainer  = GabOnTertiaryContainer,

    background           = GabBackground,
    onBackground         = GabOnBackground,

    surface              = GabSurface,
    onSurface            = GabOnSurface,
    surfaceVariant       = GabSurfaceVariant,
    onSurfaceVariant     = GabOnSurfaceVariant,

    error                = GabError,
    onError              = GabOnError,
    errorContainer       = GabErrorContainer,
    onErrorContainer     = GabOnErrorContainer,

    outline              = GabOutline,
    outlineVariant       = GabOutlineVariant,
    scrim                = GabScrim,
    inverseSurface       = GabInverseSurface,
    inverseOnSurface     = GabInverseOnSurface,
    inversePrimary       = GabInversePrimary
)

private val DarkColorScheme = darkColorScheme(
    primary              = GabDarkPrimary,
    onPrimary            = GabDarkOnPrimary,
    primaryContainer     = GabDarkPrimaryContainer,
    onPrimaryContainer   = GabDarkOnPrimaryContainer,

    secondary            = GabDarkSecondary,
    onSecondary          = GabDarkOnSecondary,
    secondaryContainer   = GabDarkSecondaryContainer,
    onSecondaryContainer = GabDarkOnSecondaryContainer,

    tertiary             = GabDarkTertiary,
    onTertiary           = GabDarkOnTertiary,
    tertiaryContainer    = GabDarkTertiaryContainer,
    onTertiaryContainer  = GabDarkOnTertiaryContainer,

    background           = GabDarkBackground,
    onBackground         = GabDarkOnBackground,

    surface              = GabDarkSurface,
    onSurface            = GabDarkOnSurface,
    surfaceVariant       = GabDarkSurfaceVariant,
    onSurfaceVariant     = GabDarkOnSurfaceVariant,

    error                = GabDarkError,
    onError              = GabDarkOnError,
    errorContainer       = GabDarkErrorContainer,
    onErrorContainer     = GabDarkOnErrorContainer,

    outline              = GabDarkOutline,
    outlineVariant       = GabDarkOutlineVariant,
    scrim                = GabScrim,
    inverseSurface       = GabDarkInverseSurface,
    inverseOnSurface     = GabDarkInverseOnSurface,
    inversePrimary       = GabDarkInversePrimary
)

@Composable
fun GabInovaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = GabTypography,
        shapes      = GabShapes,
        content     = content
    )
}
