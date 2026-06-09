package io.beanthemoonman.pokeapp.tv.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.beanthemoonman.pokeapp.domain.model.Item
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.tv.R
import io.beanthemoonman.pokeapp.tv.ui.common.TvBackBar
import io.beanthemoonman.pokeapp.tv.ui.common.TvErrorState
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.uistate.items.ItemDetailViewModel

/** TV item detail — split identity panel + effect/description (tv-screens.jsx `TVItemDetail`). */
@Composable
fun TvItemDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Box(modifier = modifier.fillMaxSize().background(PokedexColors.Background)) {
        when (val s = state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize().padding(44.dp)) { TvBackBar(stringResource(R.string.items_back)) }
            is UiState.Error -> TvErrorState(ItemAccent, stringResource(R.string.items_error_title), stringResource(R.string.items_error_body), viewModel::retry)
            is UiState.Success -> ItemDetailContent(s.data)
        }
    }
}

@Composable
private fun ItemDetailContent(item: Item) {
    Row(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(0.44f).fillMaxHeight().border(1.dp, PokedexColors.Line).padding(44.dp),
        ) {
            TvBackBar(stringResource(R.string.items_back))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                ItemTile(size = 130.dp, cornerRadius = 20.dp)
                Column {
                    Text(stringResource(item.category.labelRes()).uppercase(), color = ItemAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Text(item.name, color = PokedexColors.TextPrimary, fontSize = 34.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.items_buy).uppercase(), color = PokedexColors.TextFaint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        ItemCost(item.cost)
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight().padding(44.dp).verticalScroll(rememberScrollState())) {
            Label(stringResource(R.string.items_effect))
            Spacer(Modifier.height(10.dp))
            Text(item.shortEffect, color = PokedexColors.TextPrimary, fontSize = 16.sp, lineHeight = 26.sp)
            Spacer(Modifier.height(26.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(PokedexColors.Surface).border(1.dp, PokedexColors.Line, RoundedCornerShape(14.dp)).padding(20.dp),
            ) {
                Label(stringResource(R.string.items_description))
                Spacer(Modifier.height(9.dp))
                Text(item.flavor, color = PokedexColors.TextDim, fontSize = 14.5.sp, lineHeight = 24.sp)
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text.uppercase(), color = PokedexColors.TextFaint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
}
