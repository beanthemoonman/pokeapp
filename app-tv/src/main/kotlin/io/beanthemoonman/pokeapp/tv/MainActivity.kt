package io.beanthemoonman.pokeapp.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokedexTheme {
                PlaceholderBrowse(
                    Modifier
                        .fillMaxSize()
                        .background(PokedexColors.Background)
                )
            }
        }
    }
}

@Composable
private fun PlaceholderBrowse(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Pokédex — TV")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TypeBadge(type = Type.FIRE)
            TypeBadge(type = Type.WATER)
            TypeBadge(type = Type.GRASS)
        }
    }
}

@Preview(widthDp = 960, heightDp = 540)
@Composable
private fun PlaceholderBrowsePreview() {
    PokedexTheme { PlaceholderBrowse() }
}
