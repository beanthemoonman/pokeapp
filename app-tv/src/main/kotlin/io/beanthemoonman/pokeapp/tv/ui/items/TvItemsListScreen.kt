package io.beanthemoonman.pokeapp.tv.ui.items

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.beanthemoonman.pokeapp.domain.model.Item
import io.beanthemoonman.pokeapp.domain.model.ItemCategory
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
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.uistate.items.ItemsListData
import io.beanthemoonman.pokeapp.uistate.items.ItemsListViewModel
import io.beanthemoonman.pokeapp.uistate.items.ItemsSearchUiState

private const val ITEM_COLUMNS = 3
private const val ALL = "all"

/** TV Items dictionary — at parity with the phone Items list (tv-screens.jsx `TVItems`). */
@Composable
fun TvItemsListScreen(
    onItemClick: (Int) -> Unit,
    onNavigate: (TvNavItem) -> Unit,
    onSwitchGeneration: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemsListViewModel = hiltViewModel(),
) {
    val generation by viewModel.generation.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()

    val activeCategory = (state as? UiState.Success)?.data?.category

    TvScreenScaffold(
        active = TvNavItem.ITEMS,
        onNavigate = onNavigate,
        modifier = modifier,
        sidebar = {
            TvSidebar {
                TvFilterSection(
                    title = stringResource(R.string.items_category),
                    options = categoryOptions(),
                    activeId = activeCategory?.slug ?: ALL,
                    onSelect = { id ->
                        viewModel.selectCategory(if (id == ALL) null else ItemCategory.fromSlug(id))
                    },
                )
                generation?.let { TvGenBlock(it, onSwitchGeneration) }
            }
        },
    ) {
        TvContentHeader(
            title = stringResource(R.string.items_title),
            subtitle = generation?.let {
                stringResource(R.string.items_subtitle, (state as? UiState.Success)?.data?.items?.size ?: 0, it.label)
            } ?: "",
            trailing = {
                TvSearchField(
                    value = query,
                    onValueChange = viewModel::onQueryChange,
                    onClear = viewModel::clearSearch,
                    hint = stringResource(R.string.items_search_hint),
                    accent = ItemAccent,
                )
            },
        )

        when {
            query.isNotBlank() -> when (val s = searchState) {
                ItemsSearchUiState.Idle, ItemsSearchUiState.Loading -> ItemsGrid(loading = true, onItemClick = onItemClick)
                is ItemsSearchUiState.Results -> ItemsGrid(items = s.items, onItemClick = onItemClick)
                ItemsSearchUiState.Empty -> CenteredMessage(stringResource(R.string.browse_search_empty_body, query))
                is ItemsSearchUiState.Error -> TvErrorState(ItemAccent, stringResource(R.string.items_error_title), s.message, viewModel::retrySearch)
            }
            else -> when (val s = state) {
                is UiState.Loading -> ItemsGrid(loading = true, onItemClick = onItemClick)
                is UiState.Error -> TvErrorState(ItemAccent, stringResource(R.string.items_error_title), stringResource(R.string.items_error_body), viewModel::retry)
                is UiState.Success -> ItemsGrid(data = s.data, onItemClick = onItemClick, onLoadMore = viewModel::loadMore)
            }
        }
        TvHints(listOf(stringResource(R.string.items_hint_select), stringResource(R.string.items_hint_open), stringResource(R.string.items_hint_menu)))
    }
}

@Composable
private fun categoryOptions(): List<TvFilterOption> = buildList {
    add(TvFilterOption(ALL, stringResource(R.string.items_category_all), androidx.compose.ui.graphics.Color.White))
    ItemCategory.chips.forEach { add(TvFilterOption(it.slug, stringResource(it.labelRes()), ItemAccent)) }
}

@Composable
private fun ItemsGrid(
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    data: ItemsListData? = null,
    items: List<Item> = data?.visible ?: emptyList(),
    loading: Boolean = false,
    onLoadMore: () -> Unit = {},
) {
    val gridState = rememberLazyGridState()
    if (!loading) {
        val shouldLoadMore by remember {
            derivedStateOf {
                val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                last >= items.size - ITEM_COLUMNS * 2
            }
        }
        LaunchedEffect(shouldLoadMore, items.size) { if (shouldLoadMore) onLoadMore() }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(ITEM_COLUMNS),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (loading) {
            items(count = ITEM_COLUMNS * 3) { ItemSkeleton() }
        } else {
            items(items = items, key = { it.id }) { ItemCard(it, onItemClick) }
            if (data?.isAppending == true) items(count = ITEM_COLUMNS) { ItemSkeleton() }
        }
    }
}

@Composable
private fun ItemCard(item: Item, onClick: (Int) -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PokedexColors.Surface)
            .tvFocusRing(focused = focused, accent = ItemAccent, cornerRadius = 14.dp)
            .clickable(interactionSource = interaction, indication = null) { onClick(item.id) }
            .focusable(interactionSource = interaction)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ItemTile()
        Column(Modifier.weight(1f)) {
            Text(item.name, color = PokedexColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(stringResource(item.category.labelRes()), color = PokedexColors.TextFaint, fontSize = 11.5.sp, modifier = Modifier.padding(top = 3.dp))
        }
        ItemCost(item.cost)
    }
}

@Composable
fun ItemCost(cost: Int) {
    if (cost <= 0) {
        Text(stringResource(R.string.items_cost_none), color = PokedexColors.TextFaint, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace)
    } else {
        Text(stringResource(R.string.items_cost, "%,d".format(cost)), color = PokedexColors.TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun ItemSkeleton() {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(PokedexColors.Surface).border(1.dp, PokedexColors.Line, RoundedCornerShape(14.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SkeletonBox(modifier = Modifier.size(64.dp))
        Column(Modifier.weight(1f)) {
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(14.dp))
            SkeletonBox(modifier = Modifier.padding(top = 8.dp).fillMaxWidth(0.35f).height(10.dp))
        }
    }
}

@Composable
private fun CenteredMessage(body: String) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(body, color = PokedexColors.TextDim, fontSize = 14.sp)
    }
}
