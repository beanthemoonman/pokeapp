package io.beanthemoonman.pokeapp.tv.ui.moves

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.beanthemoonman.pokeapp.domain.model.Move
import io.beanthemoonman.pokeapp.domain.model.MoveCategory
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.tv.R
import io.beanthemoonman.pokeapp.tv.ui.common.TvContentHeader
import io.beanthemoonman.pokeapp.tv.ui.common.TvErrorState
import io.beanthemoonman.pokeapp.tv.ui.common.TvFilterOption
import io.beanthemoonman.pokeapp.tv.ui.common.TvFilterSection
import io.beanthemoonman.pokeapp.tv.ui.common.TvGenBlock
import io.beanthemoonman.pokeapp.tv.ui.common.TvHints
import io.beanthemoonman.pokeapp.tv.ui.common.TvNavItem
import io.beanthemoonman.pokeapp.tv.ui.common.TvScreenScaffold
import io.beanthemoonman.pokeapp.tv.ui.common.TvSearchField
import io.beanthemoonman.pokeapp.tv.ui.common.TvSidebar
import io.beanthemoonman.pokeapp.tv.ui.common.tvFocusRing
import io.beanthemoonman.pokeapp.ui.common.component.SkeletonBox
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadgeSize
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.color
import io.beanthemoonman.pokeapp.uistate.moves.MovesListData
import io.beanthemoonman.pokeapp.uistate.moves.MovesListViewModel
import io.beanthemoonman.pokeapp.uistate.moves.MovesSearchUiState

private const val ALL = "all"

/** TV Moves dictionary — at parity with the phone Moves list (tv-screens.jsx `TVMoves`). */
@Composable
fun TvMovesListScreen(
  onMoveClick: (Int) -> Unit,
  onNavigate: (TvNavItem) -> Unit,
  onSwitchGeneration: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MovesListViewModel = hiltViewModel(),
) {
  val generation by viewModel.generation.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val query by viewModel.query.collectAsStateWithLifecycle()
  val searchState by viewModel.searchState.collectAsStateWithLifecycle()

  val activeCategory = (state as? UiState.Success)?.data?.category

  TvScreenScaffold(
    active = TvNavItem.MOVES,
    onNavigate = onNavigate,
    modifier = modifier,
    sidebar = {
      TvSidebar {
        TvFilterSection(
          title = stringResource(R.string.moves_class),
          options = classOptions(),
          activeId = activeCategory?.name ?: ALL,
          onSelect = { id ->
            viewModel.selectCategory(
              if (id == ALL) null else MoveCategory.valueOf(
                id
              )
            )
          },
        )
        generation?.let { TvGenBlock(it, onSwitchGeneration) }
      }
    },
  ) {
    TvContentHeader(
      title = stringResource(R.string.moves_title),
      subtitle = generation?.let {
        stringResource(
          R.string.moves_subtitle,
          (state as? UiState.Success)?.data?.moves?.size ?: 0,
          it.label
        )
      } ?: "",
      trailing = {
        TvSearchField(
          value = query,
          onValueChange = viewModel::onQueryChange,
          onClear = viewModel::clearSearch,
          hint = stringResource(R.string.moves_search_hint),
          accent = MovesAccent,
        )
      },
    )

    when {
      query.isNotBlank() -> when (val s = searchState) {
        MovesSearchUiState.Idle, MovesSearchUiState.Loading -> MovesList(
          loading = true,
          onMoveClick = onMoveClick
        )

        is MovesSearchUiState.Results -> MovesList(moves = s.moves, onMoveClick = onMoveClick)
        MovesSearchUiState.Empty -> CenteredMessage(
          stringResource(
            R.string.browse_search_empty_body,
            query
          )
        )

        is MovesSearchUiState.Error -> TvErrorState(
          MovesAccent,
          stringResource(R.string.moves_error_title),
          s.message,
          viewModel::retrySearch
        )
      }

      else -> when (val s = state) {
        is UiState.Loading -> MovesList(loading = true, onMoveClick = onMoveClick)
        is UiState.Error -> TvErrorState(
          MovesAccent,
          stringResource(R.string.moves_error_title),
          stringResource(R.string.moves_error_body),
          viewModel::retry
        )

        is UiState.Success -> MovesList(
          data = s.data,
          onMoveClick = onMoveClick,
          onLoadMore = viewModel::loadMore
        )
      }
    }
    TvHints(
      listOf(
        stringResource(R.string.moves_hint_rows),
        stringResource(R.string.moves_hint_open),
        stringResource(R.string.moves_hint_menu)
      )
    )
  }
}

@Composable
private fun classOptions(): List<TvFilterOption> = buildList {
  add(
    TvFilterOption(
      ALL,
      stringResource(R.string.moves_class_all),
      androidx.compose.ui.graphics.Color.White
    )
  )
  MoveClassChips.forEach {
    add(
      TvFilterOption(
        it.name,
        stringResource(it.labelRes()),
        it.classColor()
      )
    )
  }
}

@Composable
private fun MovesList(
  onMoveClick: (Int) -> Unit,
  modifier: Modifier = Modifier,
  data: MovesListData? = null,
  moves: List<Move> = data?.visible ?: emptyList(),
  loading: Boolean = false,
  onLoadMore: () -> Unit = {},
) {
  val listState = rememberLazyListState()
  if (!loading) {
    val shouldLoadMore by remember {
      derivedStateOf {
        val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        last >= moves.size - 6
      }
    }
    LaunchedEffect(shouldLoadMore, moves.size) { if (shouldLoadMore) onLoadMore() }
  }
  LazyColumn(
    state = listState,
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 12.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (loading) {
      items(count = 8) { MoveSkeleton() }
    } else {
      items(items = moves, key = { it.id }) { MoveRow(it, onMoveClick) }
      if (data?.isAppending == true) items(count = 4) { MoveSkeleton() }
    }
  }
}

@Composable
private fun MoveRow(move: Move, onClick: (Int) -> Unit) {
  val accent = move.type?.color() ?: MovesAccent
  val interaction = remember { MutableInteractionSource() }
  val focused by interaction.collectIsFocusedAsState()
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(PokedexColors.Surface)
      .tvFocusRing(focused = focused, accent = accent, cornerRadius = 12.dp)
      .clickable(interactionSource = interaction, indication = null) { onClick(move.id) }
      .focusable(interactionSource = interaction)
      .padding(horizontal = 18.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      move.name,
      color = PokedexColors.TextPrimary,
      fontSize = 16.sp,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.weight(1f)
    )
    move.type?.let { TypeBadge(it, size = TypeBadgeSize.SM) }
    Text(
      text = stringResource(move.category.labelRes()).uppercase(),
      color = move.category.classColor(),
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.width(76.dp),
    )
    Text(
      text = move.power?.toString() ?: stringResource(R.string.moves_value_none),
      color = PokedexColors.TextPrimary,
      fontSize = 15.sp,
      fontWeight = FontWeight.SemiBold,
      fontFamily = FontFamily.Monospace,
      textAlign = TextAlign.End,
      modifier = Modifier.width(56.dp),
    )
    Text(
      text = buildString {
        append(move.accuracy?.let { stringResource(R.string.moves_acc_value, it) }
          ?: stringResource(R.string.moves_value_none))
        append(" · ")
        append(stringResource(R.string.moves_pp_value, move.pp))
      },
      color = PokedexColors.TextFaint, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
      textAlign = TextAlign.End, modifier = Modifier.width(110.dp),
    )
  }
}

@Composable
private fun MoveSkeleton() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(PokedexColors.Surface)
      .border(1.dp, PokedexColors.Line, RoundedCornerShape(12.dp))
      .padding(horizontal = 18.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    SkeletonBox(modifier = Modifier
      .fillMaxWidth(0.32f)
      .height(15.dp))
    Box(Modifier.weight(1f))
    SkeletonBox(modifier = Modifier
      .width(44.dp)
      .height(14.dp))
    SkeletonBox(modifier = Modifier
      .width(90.dp)
      .height(11.dp))
  }
}

@Composable
private fun CenteredMessage(body: String) {
  Column(
    Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(body, color = PokedexColors.TextDim, fontSize = 14.sp)
  }
}
