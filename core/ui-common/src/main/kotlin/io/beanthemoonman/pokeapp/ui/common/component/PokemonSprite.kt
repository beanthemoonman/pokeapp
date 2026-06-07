package io.beanthemoonman.pokeapp.ui.common.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

/**
 * Coil AsyncImage wrapper that shows a [SkeletonBox] shimmer while the sprite loads.
 * Derived from the Sprite spec in wireframes/components.jsx.
 */
@Composable
fun PokemonSprite(
    spriteUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(spriteUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        loading = { SkeletonBox(modifier = Modifier.fillMaxSize()) },
        error = { SkeletonBox(modifier = Modifier.fillMaxSize()) },
    )
}
