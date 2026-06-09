package io.beanthemoonman.pokeapp.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import io.beanthemoonman.pokeapp.tv.ui.nav.PokedexTvApp
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokedexTheme {
                PokedexTvApp()
            }
        }
    }
}
