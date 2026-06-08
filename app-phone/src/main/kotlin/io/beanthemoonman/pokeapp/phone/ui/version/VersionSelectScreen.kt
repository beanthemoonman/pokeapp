package io.beanthemoonman.pokeapp.phone.ui.version

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Text
import io.beanthemoonman.pokeapp.phone.R
import io.beanthemoonman.pokeapp.ui.common.component.GenerationCard
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors

@Composable
fun VersionSelectScreen(
    onGenerationChosen: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VersionSelectViewModel = hiltViewModel(),
) {
    val selectedId by viewModel.selectedId.collectAsStateWithLifecycle()
    Column(modifier = modifier.fillMaxSize().background(PokedexColors.Background)) {
        Header()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(items = viewModel.generations, key = { it.id }) { generation ->
                GenerationCard(
                    generation = generation,
                    selected = generation.id == selectedId,
                    onClick = { viewModel.select(generation.id, onGenerationChosen) },
                )
            }
        }
    }
}

@Composable
private fun Header() {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)) {
        Text(
            text = stringResource(R.string.version_eyebrow).uppercase(),
            color = PokedexColors.TextFaint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp,
        )
        Text(
            text = stringResource(R.string.version_title),
            color = PokedexColors.TextPrimary,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 6.dp),
        )
        Text(
            text = stringResource(R.string.version_subtitle),
            color = PokedexColors.TextDim,
            fontSize = 13.5.sp,
        )
    }
}
