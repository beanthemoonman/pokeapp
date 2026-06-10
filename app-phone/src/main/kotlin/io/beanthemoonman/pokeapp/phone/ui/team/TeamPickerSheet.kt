package io.beanthemoonman.pokeapp.phone.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.phone.R
import io.beanthemoonman.pokeapp.ui.common.component.PokemonSprite
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadgeSize
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.color
import io.beanthemoonman.pokeapp.uistate.team.PickerResults
import io.beanthemoonman.pokeapp.uistate.team.TeamPickerUiState

/**
 * Add/replace picker rendered as a [ModalBottomSheet]. Stateless: it only reflects the passed
 * [state] and forwards user intent through the callbacks. Renders nothing while
 * [TeamPickerUiState.Closed]; opens the sheet for [TeamPickerUiState.Open].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamPickerSheet(
  state: TeamPickerUiState,
  onQueryChange: (String) -> Unit,
  onSelect: (Pokemon) -> Unit,
  onRemove: (Int) -> Unit,
  onDismiss: () -> Unit,
) {
  val open = state as? TeamPickerUiState.Open ?: return
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = PokedexColors.Background,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
      Header(slot = open.slot, replacing = open.replacing, onRemove = onRemove)
      PickerSearchField(
        query = open.query,
        onQueryChange = onQueryChange,
        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
      )
      PickerBody(
        results = open.results,
        query = open.query,
        onSelect = onSelect,
      )
    }
  }
}

@Composable
private fun Header(slot: Int, replacing: Boolean, onRemove: (Int) -> Unit) {
  val titleRes =
    if (replacing) R.string.team_picker_title_replace else R.string.team_picker_title_add
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = stringResource(titleRes, slot + 1),
      color = PokedexColors.TextPrimary,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
    )
    if (replacing) {
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .clickable { onRemove(slot) }
          .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          imageVector = Icons.Outlined.DeleteOutline,
          contentDescription = null,
          tint = WeakColor,
          modifier = Modifier.size(18.dp),
        )
        Text(
          text = stringResource(R.string.team_slot_remove),
          color = WeakColor,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
  }
}

@Composable
private fun PickerSearchField(
  query: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val shape = RoundedCornerShape(12.dp)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(44.dp)
      .clip(shape)
      .background(PokedexColors.Surface)
      .border(1.dp, PokedexColors.Line, shape)
      .padding(horizontal = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Icon(
      imageVector = Icons.Outlined.Search,
      contentDescription = null,
      tint = PokedexColors.TextFaint,
      modifier = Modifier.size(18.dp),
    )
    BasicTextField(
      value = query,
      onValueChange = onQueryChange,
      modifier = Modifier.weight(1f),
      singleLine = true,
      textStyle = TextStyle(color = PokedexColors.TextPrimary, fontSize = 14.5.sp),
      cursorBrush = SolidColor(Type.FIRE.color()),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      decorationBox = { innerTextField ->
        if (query.isEmpty()) {
          Text(
            text = stringResource(R.string.team_picker_hint),
            color = PokedexColors.TextFaint,
            fontSize = 14.5.sp,
          )
        }
        innerTextField()
      },
    )
    if (query.isNotEmpty()) {
      Icon(
        imageVector = Icons.Outlined.Close,
        contentDescription = stringResource(R.string.team_picker_close),
        tint = PokedexColors.TextDim,
        modifier = Modifier
          .size(18.dp)
          .clip(RoundedCornerShape(50))
          .clickable { onQueryChange("") },
      )
    }
  }
}

@Composable
private fun PickerBody(results: PickerResults, query: String, onSelect: (Pokemon) -> Unit) {
  when (results) {
    PickerResults.Idle -> CenteredMessage(stringResource(R.string.team_picker_prompt))
    PickerResults.Loading -> Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(RESULTS_MAX_HEIGHT),
      contentAlignment = Alignment.Center,
    ) {
      CircularProgressIndicator(color = Type.FIRE.color(), strokeWidth = 2.dp)
    }

    PickerResults.Empty -> CenteredMessage(stringResource(R.string.team_picker_empty, query))
    PickerResults.Error -> CenteredMessage(stringResource(R.string.team_picker_error))
    is PickerResults.Results -> LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = RESULTS_MAX_HEIGHT),
      contentPadding = PaddingValues(bottom = 8.dp),
    ) {
      items(items = results.items, key = { it.id }) { p ->
        ResultRow(pokemon = p, onClick = { onSelect(p) })
      }
    }
  }
}

@Composable
private fun ResultRow(pokemon: Pokemon, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    PokemonSprite(
      spriteUrl = pokemon.spriteUrl,
      contentDescription = pokemon.name,
      modifier = Modifier
        .size(48.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(PokedexColors.Surface),
    )
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = pokemon.name,
        color = PokedexColors.TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = "#%03d".format(pokemon.id),
        color = PokedexColors.TextFaint,
        fontSize = 11.5.sp,
        fontFamily = FontFamily.Monospace,
      )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      pokemon.types.forEach { type ->
        TypeBadge(type = type, size = TypeBadgeSize.SM)
      }
    }
  }
}

@Composable
private fun CenteredMessage(text: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(RESULTS_MAX_HEIGHT)
      .padding(horizontal = 24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      color = PokedexColors.TextDim,
      fontSize = 13.5.sp,
      textAlign = TextAlign.Center,
    )
  }
}

private val RESULTS_MAX_HEIGHT = 420.dp
private val WeakColor = Color(0xFFFF6B5C)
