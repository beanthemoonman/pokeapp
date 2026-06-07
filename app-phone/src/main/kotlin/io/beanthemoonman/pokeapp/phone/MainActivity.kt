package io.beanthemoonman.pokeapp.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PokedexTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    PlaceholderHome(Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
private fun PlaceholderHome(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Pokédex — Phone")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TypeBadge(type = Type.FIRE)
            TypeBadge(type = Type.WATER)
            TypeBadge(type = Type.GRASS)
        }
    }
}

@Preview
@Composable
private fun PlaceholderHomePreview() {
    PokedexTheme { PlaceholderHome() }
}
