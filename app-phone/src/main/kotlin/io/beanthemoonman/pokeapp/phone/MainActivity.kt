package io.beanthemoonman.pokeapp.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.beanthemoonman.pokeapp.phone.ui.nav.PokedexApp
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PokedexTheme {
                PokedexApp()
            }
        }
    }
}
