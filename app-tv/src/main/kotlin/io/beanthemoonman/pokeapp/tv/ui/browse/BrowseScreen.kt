package io.beanthemoonman.pokeapp.tv.ui.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.tv.R
import io.beanthemoonman.pokeapp.tv.ui.common.TvContentHeader
import io.beanthemoonman.pokeapp.tv.ui.common.TvErrorState
import io.beanthemoonman.pokeapp.tv.ui.common.TvFireAccent
import io.beanthemoonman.pokeapp.tv.ui.common.TvGenBlock
import io.beanthemoonman.pokeapp.tv.ui.common.TvNavItem
import io.beanthemoonman.pokeapp.tv.ui.common.TvScreenScaffold
import io.beanthemoonman.pokeapp.tv.ui.common.TvSearchField
import io.beanthemoonman.pokeapp.tv.ui.common.TvSidebar
import io.beanthemoonman.pokeapp.tv.ui.common.tvFocusRing
import io.beanthemoonman.pokeapp.ui.common.component.PokemonSprite
import io.beanthemoonman.pokeapp.ui.common.component.SkeletonBox
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadgeSize
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.color
import io.beanthemoonman.pokeapp.uistate.list.PokemonListData
import io.beanthemoonman.pokeapp.uistate.list.PokemonListViewModel
import io.beanthemoonman.pokeapp.uistate.list.SearchUiState

private const val GRID_COLUMNS = 3

/** TV Browse grid — at parity with the phone Pokédex list (see tv-screens.jsx `TVBrowse`). */
@Composable
fun BrowseScreen(
  onPokemonClick: (Int) -> Unit,
  onNavigate: (TvNavItem) -> Unit,
  onSwitchGeneration: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: PokemonListViewModel = hiltViewModel(),
) {
  val generation by viewModel.generation.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val query by viewModel.query.collectAsStateWithLifecycle()
  val searchState by viewModel.searchState.collectAsStateWithLifecycle()

  TvScreenScaffold(
    active = TvNavItem.DEX,
    onNavigate = onNavigate,
    modifier = modifier,
    sidebar = {
      TvSidebar {
        generation?.let { TvGenBlock(it, onSwitchGeneration) }
      }
    },
  ) {
    TvContentHeader(
      title = stringResourceSafe(R.string.browse_title),
      subtitle = generation?.let {
        androidx.compose.ui.res.stringResource(R.string.browse_subtitle, it.dexEnd, it.label)
      } ?: "",
      trailing = {
        TvSearchField(
          value = query,
          onValueChange = viewModel::onQueryChange,
          onClear = viewModel::clearSearch,
          hint = stringResourceSafe(R.string.browse_search_hint),
          accent = TvFireAccent,
        )
      },
    )

    when {
      query.isNotBlank() -> SearchResults(
        searchState,
        viewModel::retrySearch,
        query,
        onPokemonClick
      )

      else -> when (val s = state) {
        is UiState.Loading -> PokemonGrid(loading = true, onPokemonClick = onPokemonClick)
        is UiState.Error -> TvErrorState(
          accent = TvFireAccent,
          title = stringResourceSafe(R.string.browse_error_title),
          body = stringResourceSafe(R.string.browse_error_body),
          onRetry = viewModel::retry,
        )

        is UiState.Success -> PokemonGrid(
          data = s.data,
          onPokemonClick = onPokemonClick,
          onLoadMore = viewModel::loadMore,
        )
      }
    }
  }
}

@Composable
private fun SearchResults(
  searchState: SearchUiState,
  onRetry: () -> Unit,
  query: String,
  onPokemonClick: (Int) -> Unit,
) {
  when (searchState) {
    SearchUiState.Idle, SearchUiState.Loading -> PokemonGrid(
      loading = true,
      onPokemonClick = onPokemonClick
    )

    is SearchUiState.Results -> PokemonGrid(
      items = searchState.items,
      onPokemonClick = onPokemonClick
    )

    SearchUiState.Empty -> CenteredMessage(
      stringResourceSafe(R.string.browse_search_empty_title),
      androidx.compose.ui.res.stringResource(R.string.browse_search_empty_body, query),
    )

    is SearchUiState.Error -> TvErrorState(
      accent = TvFireAccent,
      title = stringResourceSafe(R.string.browse_error_title),
      body = stringResourceSafe(R.string.browse_search_error),
      onRetry = onRetry,
    )
  }
}

@Composable
private fun PokemonGrid(
  onPokemonClick: (Int) -> Unit,
  modifier: Modifier = Modifier,
  data: PokemonListData? = null,
  items: List<Pokemon> = data?.items ?: emptyList(),
  loading: Boolean = false,
  onLoadMore: () -> Unit = {},
) {
  val gridState = rememberLazyGridState()
  if (!loading) {
    val shouldLoadMore by remember {
      derivedStateOf {
        val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        last >= items.size - GRID_COLUMNS * 2
      }
    }
    LaunchedEffect(shouldLoadMore, items.size) { if (shouldLoadMore) onLoadMore() }
  }

  LazyVerticalGrid(
    columns = GridCells.Fixed(GRID_COLUMNS),
    state = gridState,
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 18.dp),
    horizontalArrangement = Arrangement.spacedBy(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    if (loading) {
      items(count = GRID_COLUMNS * 3) { SkeletonCard() }
    } else {
      items(items = items, key = { it.id }) { p -> PokemonCard(p, onPokemonClick) }
      if (data?.isAppending == true) {
        items(count = GRID_COLUMNS) { SkeletonCard() }
      }
    }
  }
}

@Composable
private fun PokemonCard(pokemon: Pokemon, onClick: (Int) -> Unit) {
  val accent = pokemon.types.first().color()
  val interaction = remember { MutableInteractionSource() }
  val focused by interaction.collectIsFocusedAsState()
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(14.dp))
      .background(PokedexColors.Surface)
      .tvFocusRing(focused = focused, accent = accent, cornerRadius = 14.dp)
      .clickable(interactionSource = interaction, indication = null) { onClick(pokemon.id) }
      .focusable(interactionSource = interaction)
      .padding(13.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = androidx.compose.ui.res.stringResource(R.string.dex_number, pokemon.id),
      color = PokedexColors.TextFaint,
      fontSize = 11.sp,
      modifier = Modifier.align(Alignment.End),
    )
    PokemonSprite(
      spriteUrl = pokemon.spriteUrl,
      contentDescription = androidx.compose.ui.res.stringResource(R.string.sprite_cd, pokemon.name),
      modifier = Modifier.size(76.dp),
    )
    Text(
      text = pokemon.name,
      color = PokedexColors.TextPrimary,
      fontSize = 15.sp,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(top = 9.dp, bottom = 9.dp),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
      pokemon.types.forEach { TypeBadge(it, size = TypeBadgeSize.SM) }
    }
  }
}

@Composable
private fun SkeletonCard() {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(14.dp))
      .background(PokedexColors.Surface)
      .border(1.dp, PokedexColors.Line, RoundedCornerShape(14.dp))
      .padding(13.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    SkeletonBox(modifier = Modifier.size(76.dp))
    SkeletonBox(modifier = Modifier
      .padding(top = 11.dp)
      .fillMaxWidth(0.7f)
      .height(13.dp))
    SkeletonBox(modifier = Modifier
      .padding(top = 10.dp)
      .size(width = 48.dp, height = 18.dp))
  }
}

@Composable
private fun CenteredMessage(title: String, body: String) {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      title,
      color = PokedexColors.TextPrimary,
      fontSize = 18.sp,
      fontWeight = FontWeight.SemiBold
    )
    Text(
      body,
      color = PokedexColors.TextDim,
      fontSize = 14.sp,
      modifier = Modifier.padding(top = 6.dp)
    )
  }
}

/** Small indirection so screen-body string lookups stay terse. */
@Composable
private fun stringResourceSafe(id: Int): String = androidx.compose.ui.res.stringResource(id)
