package io.beanthemoonman.pokeapp.phone.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.beanthemoonman.pokeapp.domain.model.Item
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.phone.R
import io.beanthemoonman.pokeapp.ui.common.component.SkeletonBox
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors

@Composable
fun ItemDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(modifier = modifier.fillMaxSize().background(PokedexColors.Background)) {
        BackBar(onBack = onBack)
        when (state) {
            is UiState.Loading -> DetailLoading()
            is UiState.Error -> DetailError(onRetry = viewModel::retry)
            is UiState.Success -> DetailLoaded(item = (state as UiState.Success<Item>).data)
        }
    }
}

@Composable
private fun BackBar(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(PokedexColors.Surface)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.detail_back),
                tint = PokedexColors.TextPrimary,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun DetailLoaded(item: Item) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        HeaderCard(item = item)
        SectionLabel(text = stringResource(R.string.item_effect), modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        Text(
            text = item.shortEffect.ifBlank { stringResource(R.string.item_no_effect) },
            color = PokedexColors.TextPrimary,
            fontSize = 14.5.sp,
            lineHeight = 22.sp,
        )
        if (item.flavor.isNotBlank()) {
            Column(
                modifier = Modifier
                    .padding(top = 22.dp, bottom = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PokedexColors.Surface)
                    .border(1.dp, PokedexColors.Line, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                SectionLabel(text = stringResource(R.string.item_description), modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = item.flavor,
                    color = PokedexColors.TextDim,
                    fontSize = 13.5.sp,
                    lineHeight = 21.sp,
                )
            }
        }
    }
}

@Composable
private fun HeaderCard(item: Item) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(ItemAccent.copy(alpha = 0.14f), PokedexColors.Surface),
                )
            )
            .border(1.dp, ItemAccent.copy(alpha = 0.25f), shape)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ItemIcon(size = 84.dp, cornerRadius = 14.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(item.category.labelRes()).uppercase(),
                color = ItemAccent,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
            Text(
                text = item.name,
                color = PokedexColors.TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp,
                modifier = Modifier.padding(top = 3.dp, bottom = 8.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.item_buy).uppercase(),
                    color = PokedexColors.TextFaint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                )
                CostTag(cost = item.cost)
            }
        }
    }
}

@Composable
private fun CostTag(cost: Int) {
    if (cost <= 0) {
        Text(
            text = stringResource(R.string.item_cost_none),
            color = PokedexColors.TextFaint,
            fontSize = 12.5.sp,
            fontFamily = FontFamily.Monospace,
        )
    } else {
        Text(
            text = stringResource(R.string.item_cost, "%,d".format(cost)),
            color = PokedexColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = PokedexColors.TextFaint,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
        modifier = modifier,
    )
}

@Composable
private fun DetailLoading() {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        SkeletonBox(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(116.dp),
            cornerRadius = 16.dp,
        )
        SkeletonBox(modifier = Modifier.padding(top = 24.dp).size(width = 80.dp, height = 12.dp), cornerRadius = 4.dp)
        SkeletonBox(modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(14.dp), cornerRadius = 4.dp)
        SkeletonBox(modifier = Modifier.fillMaxWidth(0.7f).padding(top = 8.dp).height(14.dp), cornerRadius = 4.dp)
    }
}

@Composable
private fun DetailError(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = ItemAccent,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = stringResource(R.string.item_detail_error_title),
            color = PokedexColors.TextPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.item_detail_error_body),
            color = PokedexColors.TextDim,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = ItemAccent,
                contentColor = ItemOnAccent,
            ),
        ) {
            Text(text = stringResource(R.string.items_retry), fontWeight = FontWeight.Bold)
        }
    }
}
