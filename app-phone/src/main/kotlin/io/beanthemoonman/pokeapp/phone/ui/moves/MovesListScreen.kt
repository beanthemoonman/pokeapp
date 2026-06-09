package io.beanthemoonman.pokeapp.phone.ui.moves

import io.beanthemoonman.pokeapp.uistate.moves.MovesListData
import io.beanthemoonman.pokeapp.uistate.moves.MovesListViewModel
import io.beanthemoonman.pokeapp.uistate.moves.MovesSearchUiState

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.domain.model.Move
import io.beanthemoonman.pokeapp.domain.model.MoveCategory
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.phone.R
import io.beanthemoonman.pokeapp.ui.common.component.SkeletonBox
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadgeSize
import io.beanthemoonman.pokeapp.ui.common.component.VersionChip
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors

@Composable
fun MovesListScreen(
    onMoveClick: (Int) -> Unit,
    onSwitchGeneration: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovesListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val generation by viewModel.generation.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    MovesListContent(
        state = state,
        generation = generation,
        query = query,
        searchState = searchState,
        onQueryChange = viewModel::onQueryChange,
        onClearSearch = viewModel::clearSearch,
        onRetrySearch = viewModel::retrySearch,
        onCategorySelected = viewModel::selectCategory,
        onMoveClick = onMoveClick,
        onRetry = viewModel::retry,
        onLoadMore = viewModel::loadMore,
        onSwitchGeneration = onSwitchGeneration,
        modifier = modifier,
    )
}

@Composable
private fun MovesListContent(
    state: UiState<MovesListData>,
    generation: Generation?,
    query: String,
    searchState: MovesSearchUiState,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onRetrySearch: () -> Unit,
    onCategorySelected: (MoveCategory?) -> Unit,
    onMoveClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onSwitchGeneration: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = (state as? UiState.Success)?.data
    Column(modifier = modifier.fillMaxSize().background(PokedexColors.Background)) {
        MovesHeader(
            loadedCount = data?.moves?.size ?: 0,
            generation = generation,
            query = query,
            onQueryChange = onQueryChange,
            onClearSearch = onClearSearch,
            onSwitchGeneration = onSwitchGeneration,
        )
        // Class chips are hidden during an active search (results span all classes).
        if (searchState is MovesSearchUiState.Idle) {
            ClassChips(
                active = data?.category,
                onCategorySelected = onCategorySelected,
            )
        }
        when (searchState) {
            is MovesSearchUiState.Idle -> when (state) {
                is UiState.Loading -> LoadingList()
                is UiState.Error -> ErrorState(onRetry = onRetry)
                is UiState.Success -> LoadedList(
                    data = state.data,
                    onMoveClick = onMoveClick,
                    onLoadMore = onLoadMore,
                    onRetryAppend = onRetry,
                )
            }
            is MovesSearchUiState.Loading -> LoadingList()
            is MovesSearchUiState.Empty -> SearchEmptyState(query = query)
            is MovesSearchUiState.Error -> SearchErrorState(onRetry = onRetrySearch)
            is MovesSearchUiState.Results -> ResultsList(moves = searchState.moves, onMoveClick = onMoveClick)
        }
    }
}

@Composable
private fun MovesHeader(
    loadedCount: Int,
    generation: Generation?,
    query: String,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSwitchGeneration: () -> Unit,
) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp)) {
        if (generation != null) {
            VersionChip(
                generation = generation,
                onClick = onSwitchGeneration,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.moves_title),
                    color = PokedexColors.TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    text = stringResource(R.string.moves_subtitle, generation?.label ?: ""),
                    color = PokedexColors.TextFaint,
                    fontSize = 11.5.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Text(
                text = stringResource(R.string.moves_count, loadedCount),
                color = PokedexColors.TextDim,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onClearSearch = onClearSearch,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
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
            cursorBrush = SolidColor(MovesAccent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.moves_search_hint),
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
                contentDescription = stringResource(R.string.moves_search_clear),
                tint = PokedexColors.TextDim,
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onClearSearch),
            )
        }
    }
}

@Composable
private fun ClassChips(
    active: MoveCategory?,
    onCategorySelected: (MoveCategory?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Chip(
            label = stringResource(R.string.move_class_all),
            color = PokedexColors.TextPrimary,
            selected = active == null,
            onClick = { onCategorySelected(null) },
        )
        MoveClassChips.forEach { category ->
            Chip(
                label = stringResource(category.labelRes()),
                color = category.classColor(),
                selected = active == category,
                onClick = { onCategorySelected(category) },
            )
        }
    }
}

@Composable
private fun Chip(label: String, color: androidx.compose.ui.graphics.Color, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .then(
                if (selected) Modifier.background(color)
                else Modifier.background(PokedexColors.Surface).border(1.dp, PokedexColors.Line, shape)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            color = if (selected) MovesOnAccent else PokedexColors.TextDim,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp,
        )
    }
}

@Composable
private fun LoadedList(
    data: MovesListData,
    onMoveClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onRetryAppend: () -> Unit,
) {
    val visible = data.visible
    val listState = rememberLazyListState()

    val visibleCount = visible.size
    val canPage = !data.endReached && !data.isAppending && !data.appendError
    val shouldLoadMore by remember(visibleCount, canPage) {
        derivedStateOf {
            canPage &&
                (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) >= visibleCount - LOAD_MORE_THRESHOLD
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    if (visible.isEmpty() && data.endReached) {
        ClassEmptyState()
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(items = visible, key = { it.id }) { move ->
            MoveRow(move = move, onClick = { onMoveClick(move.id) })
            Divider()
        }
        when {
            data.appendError -> item { AppendError(onRetry = onRetryAppend) }
            data.isAppending -> items(count = 3) { SkeletonRow(); Divider() }
        }
    }
}

@Composable
private fun ResultsList(moves: List<Move>, onMoveClick: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(items = moves, key = { it.id }) { move ->
            MoveRow(move = move, onClick = { onMoveClick(move.id) })
            Divider()
        }
    }
}

@Composable
private fun MoveRow(move: Move, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = move.name,
                color = PokedexColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.padding(top = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                move.type?.let { TypeBadge(type = it, size = TypeBadgeSize.SM) }
                Text(
                    text = stringResource(move.category.labelRes()).uppercase(),
                    color = move.category.classColor(),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = move.power?.toString() ?: stringResource(R.string.move_value_none),
                color = PokedexColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = stringResource(
                    R.string.move_meta,
                    move.accuracy?.let { stringResource(R.string.move_accuracy, it) }
                        ?: stringResource(R.string.move_value_none),
                    move.pp,
                ),
                color = PokedexColors.TextFaint,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun LoadingList() {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(8) {
            SkeletonRow()
            Divider()
        }
    }
}

@Composable
private fun SkeletonRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.45f).height(14.dp), cornerRadius = 4.dp)
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.32f).height(14.dp), cornerRadius = 999.dp)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkeletonBox(modifier = Modifier.size(width = 34.dp, height = 13.dp), cornerRadius = 4.dp)
            SkeletonBox(modifier = Modifier.size(width = 54.dp, height = 9.dp), cornerRadius = 4.dp)
        }
    }
}

@Composable
private fun AppendError(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = PokedexColors.SurfaceRaised,
                contentColor = PokedexColors.TextPrimary,
            ),
        ) {
            Text(text = stringResource(R.string.moves_load_more_retry))
        }
    }
}

@Composable
private fun ClassEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.moves_class_empty),
            color = PokedexColors.TextDim,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SearchEmptyState(query: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = PokedexColors.TextFaint,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = stringResource(R.string.moves_search_empty_title),
            color = PokedexColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.moves_search_empty_body, query),
            color = PokedexColors.TextDim,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SearchErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MovesAccent,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = stringResource(R.string.moves_search_error),
            color = PokedexColors.TextDim,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = PokedexColors.SurfaceRaised,
                contentColor = PokedexColors.TextPrimary,
            ),
        ) {
            Text(text = stringResource(R.string.moves_retry))
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PokedexColors.SurfaceRaised),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = MovesAccent,
                modifier = Modifier.size(30.dp),
            )
        }
        Text(
            text = stringResource(R.string.moves_error_title),
            color = PokedexColors.TextPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.moves_error_body),
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

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(PokedexColors.Line),
    )
}

/** Trigger the next page once the user is within this many rows of the end. */
private const val LOAD_MORE_THRESHOLD = 6
