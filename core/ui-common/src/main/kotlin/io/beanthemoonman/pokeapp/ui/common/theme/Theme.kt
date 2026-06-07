package io.beanthemoonman.pokeapp.ui.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Dark-only Pokédex theme, shared by phone and TV targets.
 * No light theme by design (see CLAUDE.md).
 */
private val PokedexDarkColorScheme = darkColorScheme(
    background = PokedexColors.Background,
    surface = PokedexColors.Surface,
    surfaceVariant = PokedexColors.SurfaceRaised,
    onBackground = PokedexColors.TextPrimary,
    onSurface = PokedexColors.TextPrimary,
    onSurfaceVariant = PokedexColors.TextDim,
    outline = PokedexColors.Line,
)

@Composable
fun PokedexTheme(
    // Parameter retained for API symmetry; theme is always dark.
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PokedexDarkColorScheme,
        typography = PokedexTypography,
        content = content
    )
}
