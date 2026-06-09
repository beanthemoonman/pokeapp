package io.beanthemoonman.pokeapp.phone.ui.list

import io.beanthemoonman.pokeapp.uistate.list.PokemonListData
import io.beanthemoonman.pokeapp.uistate.list.PokemonListViewModel
import io.beanthemoonman.pokeapp.uistate.list.SearchUiState

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import timber.log.Timber
import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.phone.R
import io.beanthemoonman.pokeapp.ui.common.component.PokemonSprite
import io.beanthemoonman.pokeapp.ui.common.component.SkeletonBox
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadgeSize
import io.beanthemoonman.pokeapp.ui.common.component.VersionChip
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.color

@Composable
fun PokemonListScreen(
    onPokemonClick: (Int) -> Unit,
    onSwitchGeneration: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val generation by viewModel.generation.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    PokemonListContent(
        state = state,
        generation = generation,
        query = query,
        searchState = searchState,
        onQueryChange = viewModel::onQueryChange,
        onClearSearch = viewModel::clearSearch,
        onRetrySearch = viewModel::retrySearch,
        onPokemonClick = onPokemonClick,
        onRetry = viewModel::retry,
        onLoadMore = viewModel::loadMore,
        onSwitchGeneration = onSwitchGeneration,
        modifier = modifier,
    )
}

@Composable
private fun PokemonListContent(
    state: UiState<PokemonListData>,
    generation: Generation?,
    query: String,
    searchState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onRetrySearch: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onSwitchGeneration: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(PokedexColors.Background)) {
        val loadedCount = (state as? UiState.Success)?.data?.items?.size ?: 0
        ListHeader(
            loadedCount = loadedCount,
            generation = generation,
            query = query,
            onQueryChange = onQueryChange,
            onClearSearch = onClearSearch,
            onSwitchGeneration = onSwitchGeneration,
        )
        // While a query is active the search results replace the paged dex; the paged
        // list (and its scroll position) stays mounted underneath only when Idle.
        when (searchState) {
            is SearchUiState.Idle -> when (state) {
                is UiState.Loading -> LoadingList()
                is UiState.Error -> ErrorState(message = state.message, onRetry = onRetry)
                is UiState.Success -> LoadedList(
                    data = state.data,
                    resetKey = generation?.id,
                    onPokemonClick = onPokemonClick,
                    onLoadMore = onLoadMore,
                    onRetryAppend = onRetry,
                )
            }
            is SearchUiState.Loading -> LoadingList()
            is SearchUiState.Empty -> SearchEmptyState(query = query)
            is SearchUiState.Error -> SearchErrorState(onRetry = onRetrySearch)
            is SearchUiState.Results -> SearchResultsList(
                items = searchState.items,
                onPokemonClick = onPokemonClick,
            )
        }
    }
}

@Composable
private fun ListHeader(
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
                    text = stringResource(R.string.list_title),
                    color = PokedexColors.TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    text = stringResource(R.string.list_subtitle, generation?.label ?: ""),
                    color = PokedexColors.TextFaint,
                    fontSize = 11.5.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Text(
                text = stringResource(R.string.list_count, loadedCount, generation?.dexEnd ?: 0),
                color = PokedexColors.TextDim,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
        SearchBar(
            query = query,
            dexEnd = generation?.dexEnd ?: 0,
            onQueryChange = onQueryChange,
            onClearSearch = onClearSearch,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    dexEnd: Int,
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
            textStyle = TextStyle(
                color = PokedexColors.TextPrimary,
                fontSize = 14.5.sp,
            ),
            cursorBrush = SolidColor(Type.FIRE.color()),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.list_search_hint),
                        color = PokedexColors.TextFaint,
                        fontSize = 14.5.sp,
                    )
                }
                innerTextField()
            },
        )
        if (query.isEmpty()) {
            Text(
                text = dexEnd.toString(),
                color = PokedexColors.TextFaint,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.list_search_clear),
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
private fun SearchResultsList(
    items: List<Pokemon>,
    onPokemonClick: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(items = items, key = { it.id }) { p ->
            ListRow(pokemon = p, onClick = { onPokemonClick(p.id) })
            Divider()
        }
    }
}

@Composable
private fun SearchEmptyState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
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
            text = stringResource(R.string.list_search_empty_title),
            color = PokedexColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.list_search_empty_body, query),
            color = PokedexColors.TextDim,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SearchErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = Type.FIRE.color(),
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = stringResource(R.string.list_search_error),
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
            Text(text = stringResource(R.string.list_retry))
        }
    }
}

@Composable
private fun LoadedList(
    data: PokemonListData,
    resetKey: Any?,
    onPokemonClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onRetryAppend: () -> Unit,
) {
    val listState = rememberLazyListState()

    // When the generation changes the list is a fresh dex — jump back to the top so a
    // restored deep scroll position doesn't immediately trigger paging.
    LaunchedEffect(resetKey) {
        Timber.d("LoadedList resetKey=%s -> scrollToItem(0)", resetKey)
        listState.scrollToItem(0)
    }

    // Request the next page as the user nears the end of what's loaded. Keyed on the
    // current item count + paging flags so the derivation never closes over stale data.
    val itemCount = data.items.size
    val canPage = !data.endReached && !data.isAppending && !data.appendError
    val shouldLoadMore by remember(itemCount, canPage) {
        derivedStateOf {
            canPage &&
                (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) >= itemCount - LOAD_MORE_THRESHOLD
        }
    }
    LaunchedEffect(shouldLoadMore) {
        Timber.v("shouldLoadMore=%b itemCount=%d canPage=%b lastVisible=%d", shouldLoadMore, itemCount, canPage, listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1)
        if (shouldLoadMore) {
            Timber.d("LoadedList requesting next page (itemCount=%d)", itemCount)
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(items = data.items, key = { it.id }) { p ->
            ListRow(pokemon = p, onClick = { onPokemonClick(p.id) })
            Divider()
        }
        when {
            data.appendError -> item { AppendError(onRetry = onRetryAppend) }
            data.isAppending -> items(count = 3) { SkeletonRow(); Divider() }
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
            Text(text = stringResource(R.string.list_load_more_retry))
        }
    }
}

@Composable
private fun ListRow(pokemon: Pokemon, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PokemonSprite(
            spriteUrl = pokemon.spriteUrl,
            contentDescription = stringResource(R.string.sprite_cd, pokemon.name),
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PokedexColors.Surface),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pokemon.name,
                color = PokedexColors.TextPrimary,
                fontSize = 16.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = dexNumber(pokemon.id),
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
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SkeletonBox(modifier = Modifier.size(52.dp), cornerRadius = 10.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.52f).height(14.dp), cornerRadius = 4.dp)
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.28f).height(10.dp), cornerRadius = 4.dp)
        }
        SkeletonBox(modifier = Modifier.size(width = 54.dp, height = 20.dp), cornerRadius = 999.dp)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
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
                tint = Type.FIRE.color(),
                modifier = Modifier.size(30.dp),
            )
        }
        Text(
            text = stringResource(R.string.list_error_title),
            color = PokedexColors.TextPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.list_error_body),
            color = PokedexColors.TextDim,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Type.FIRE.color(),
                contentColor = PokedexColors.Background,
            ),
        ) {
            Text(text = stringResource(R.string.list_retry), fontWeight = FontWeight.Bold)
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

/** Formats a national dex id as a zero-padded `#001` label. */
private fun dexNumber(id: Int): String = "#%03d".format(id)

/** Trigger the next page once the user is within this many rows of the end. */
private const val LOAD_MORE_THRESHOLD = 6
