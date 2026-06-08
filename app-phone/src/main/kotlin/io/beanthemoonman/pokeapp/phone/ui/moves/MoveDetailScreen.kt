package io.beanthemoonman.pokeapp.phone.ui.moves

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
import io.beanthemoonman.pokeapp.domain.model.Move
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.phone.R
import io.beanthemoonman.pokeapp.ui.common.component.SkeletonBox
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadgeSize
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.color

@Composable
fun MoveDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MoveDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(modifier = modifier.fillMaxSize().background(PokedexColors.Background)) {
        BackBar(onBack = onBack)
        when (state) {
            is UiState.Loading -> DetailLoading()
            is UiState.Error -> DetailError(onRetry = viewModel::retry)
            is UiState.Success -> DetailLoaded(move = (state as UiState.Success<Move>).data)
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
private fun DetailLoaded(move: Move) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        HeaderCard(move = move)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MoveStat(
                label = stringResource(R.string.move_stat_power),
                value = move.power?.toString() ?: stringResource(R.string.move_value_none),
                modifier = Modifier.weight(1f),
            )
            MoveStat(
                label = stringResource(R.string.move_stat_acc),
                value = move.accuracy?.let { stringResource(R.string.move_accuracy, it) }
                    ?: stringResource(R.string.move_value_none),
                modifier = Modifier.weight(1f),
            )
            MoveStat(
                label = stringResource(R.string.move_stat_pp),
                value = move.pp.toString(),
                modifier = Modifier.weight(1f),
            )
        }
        SectionLabel(text = stringResource(R.string.move_effect), modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        Text(
            text = move.shortEffect.ifBlank { stringResource(R.string.move_no_effect) },
            color = PokedexColors.TextPrimary,
            fontSize = 14.5.sp,
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun HeaderCard(move: Move) {
    val accent = move.type?.color() ?: MovesAccent
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(accent.copy(alpha = 0.16f), PokedexColors.Surface),
                )
            )
            .border(1.dp, accent.copy(alpha = 0.25f), shape)
            .padding(18.dp),
    ) {
        Text(
            text = move.name,
            color = PokedexColors.TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            move.type?.let { TypeBadge(type = it, size = TypeBadgeSize.MD) }
            Text(
                text = stringResource(move.category.labelRes()).uppercase(),
                color = move.category.classColor(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
            )
        }
    }
}

@Composable
private fun MoveStat(label: String, value: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(PokedexColors.Surface)
            .border(1.dp, PokedexColors.Line, shape)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            color = PokedexColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
        Text(
            text = label.uppercase(),
            color = PokedexColors.TextFaint,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.0.sp,
            modifier = Modifier.padding(top = 4.dp),
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
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(108.dp),
            cornerRadius = 16.dp,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            repeat(3) {
                SkeletonBox(modifier = Modifier.weight(1f).height(64.dp), cornerRadius = 12.dp)
            }
        }
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
            tint = MovesAccent,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = stringResource(R.string.move_detail_error_title),
            color = PokedexColors.TextPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.move_detail_error_body),
            color = PokedexColors.TextDim,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MovesAccent,
                contentColor = MovesOnAccent,
            ),
        ) {
            Text(text = stringResource(R.string.moves_retry), fontWeight = FontWeight.Bold)
        }
    }
}
