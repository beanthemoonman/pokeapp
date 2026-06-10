# Claude Changelog

## 2026-06-07

- Determined Android TV OS 11 maps to Android 11 = API level 30, and set that as the project's `minSdk` floor.
- Updated `CLAUDE.md` app-phone and app-tv target config from `minSdk 26` to `minSdk 30`.
- Updated `app/build.gradle.kts` from `minSdk = 34` to `minSdk = 30`.
- Left `.idea/caches/deviceStreaming.xml` (IDE emulator cache, api=34) untouched as it is not project documentation/config.

### Foundation pass (multi-module setup + core/domain + core/ui-common)

- Restructured the single `:app` module into the documented 5-module layout: `app-phone`, `app-tv`, `core:data`, `core:domain`, `core:ui-common`. Updated `settings.gradle.kts`; removed old `app/`.
- Pinned a known-good toolchain in `gradle/libs.versions.toml`: AGP 9.2.1 (kept), Kotlin 2.2.20, KSP 2.2.20-2.0.2, Compose BOM 2025.05.00, Hilt 2.56.1, Retrofit 2.11.0, Room 2.7.1, Coil 2.7.0, TV Compose 1.0.0, Navigation 2.9.0, Lifecycle 2.9.0. Added catalog entries for all libs/plugins needed in later passes.
- Added `android.useAndroidX=true` / `android.nonTransitiveRClass=true` to `gradle.properties`.
- AGP 9 provides built-in Kotlin support: do NOT apply `org.jetbrains.kotlin.android`; the Compose compiler plugin is still applied separately; Kotlin options go in a `kotlin {}` block nested inside `android {}`. All Android module build files follow this.
- `core:domain` (pure Kotlin JVM): `Pokemon`/`Stats`/`Type`, `UiState`, `PokemonRepository` interface, `TypeEffectivenessMatrix` (encoded from wireframes/data.js CHART) + `TypeEffectivenessMatrixTest`.
- `core:ui-common` (Android library, Compose): dark-only `PokedexTheme`, surface tokens + 18 type color tokens + `Type.color()`/`Type.onColor()` (values from wireframes), and shared components `TypeBadge`, `StatBar`, `PokemonSprite`, `SkeletonBox`.
- `app-phone` / `app-tv` skeletons: correct manifests (phone MAIN/LAUNCHER; TV adds LEANBACK_LAUNCHER + leanback/television `uses-feature` + banner), placeholder Compose screens, strings/colors/themes, vector launcher icons.
- `core:data` created as a stub module (build file only) — data layer implemented in a later pass.
- Decision logged: switched Room/Hilt annotation processing from kapt to KSP; updated `CLAUDE.md` Key Dependencies accordingly (Hilt `ksp(...hilt-compiler...)`, Room `ksp(...room-compiler...)`).
- Design-token note: `CLAUDE.md`'s inline example color `Fire = 0xFFFF6B35` is illustrative; actual values derive from the wireframes (Fire = `#FF7A33`, etc.) per the "derive all colors from the wireframes" rule.
- Verified: `gradlew :core:domain:test :app-phone:assembleDebug :app-tv:assembleDebug` → BUILD SUCCESSFUL; domain unit tests pass.

### Data layer + Pokédex List screen pass

- **core:data** implemented end to end:
  - Retrofit `PokeApiService` (base `https://pokeapi.co/api/v2/`, no auth): `getPokemonList(limit, offset)` and `getPokemonDetail(id)`, with DTOs (`PokemonListResponseDto`, `NamedApiResourceDto` with id-from-url, `PokemonDetailDto`, sprites/types/stats DTOs; `official-artwork` mapped via `@SerializedName`).
  - Room `PokedexDatabase` (v1) with flat entities `PokemonEntity` (types + stats serialized via `Converters`), `TypeEntity`, `TeamEntity`; `PokemonDao` (upsert/getById/pageFlow/count) and `TeamDao` (team flow + upsert slot). All `@Query` use no implicit comma joins.
  - `Converters` for `List<Type>` and `List<Int>` (comma-joined).
  - `PokemonRepositoryImpl` (`@Singleton`): Room is the single source of truth — list reads come from Room, network refreshes the cache best-effort (chunked detail fetch, 12 at a time); detail is cache-first; team built from team rows joined to cached pokemon in code; offline/empty falls back to cache.
  - Mappers `PokemonDetailDto.toEntity` / `PokemonEntity.toDomain`; added `Type.fromApiName` to the domain enum for API-slug mapping.
  - Hilt DI: `DataModule` (Gson/Retrofit/PokeApiService/PokedexDatabase/DAOs) and `RepositoryModule` (`@Binds` repository).
- **Toolchain fixes for AGP 9 + Hilt/KSP:**
  - Bumped Hilt `2.56.1 → 2.59.2` (2.56–2.58 Gradle plugins fail on AGP 9 with "Android BaseExtension not found"; 2.59.x is the first AGP-9-compatible line). Updated `CLAUDE.md` Key Dependencies.
  - Added `android.disallowKotlinSourceSets=false` to `gradle.properties` so KSP can register generated sources under AGP 9 built-in Kotlin.
  - Added `javax.inject:javax.inject:1` to `core:domain` so use cases can be `@Inject`-constructed.
- **core:domain**: added `GetPokemonListUseCase` and `GetPokemonDetailUseCase`.
- **app-phone**: wired Hilt (`@HiltAndroidApp PokedexApplication`, `@AndroidEntryPoint MainActivity`, `INTERNET` permission), Navigation Compose with a bottom-nav `Scaffold` (`NavDestination` sealed class: Dex/Team/Types tabs + Detail route).
  - **Pokédex List screen** (`ui/list/`) built from `wireframes/phone-list.jsx`: header (title/KANTO·GEN I/count), search bar (visual), `LazyColumn` rows (sprite + name + `#001` + `TypeBadge`s), all three `UiState` cases — skeleton list for Loading, wireframe error state with Retry for Error. `PokemonListViewModel` (`@HiltViewModel`) exposes `StateFlow<UiState<List<Pokemon>>>`; treats empty page as Error.
  - Team/Types tabs and Detail are placeholders for upcoming passes.
  - All user-facing strings in `strings.xml`.
- **Tests**: `PokemonListViewModelTest` covers Success / empty→Error / flow-throws→Error via a fake repository + real use case.
- Verified: `gradlew :core:data:assembleDebug :app-phone:assembleDebug :core:domain:test :app-phone:testDebugUnitTest` → BUILD SUCCESSFUL; all unit tests pass, no warnings.

### Wireframe rework — root generation/version selector

Reworked the wireframes so a **generation selector is the root screen**; the chosen generation is global context for the rest of the app. Decisions (with the user): unit = **Generation**; dex = **cumulative National Dex #1..N through the gen**; type chart = **per-generation**.

- **`wireframes/data.js`**:
  - Added `GENERATIONS` (Gen I–IX: region, cumulative `dexEnd`, placeholder accent from existing type tokens, bundled `versions`), `genById`, `currentGen` (sample = 1), `dexForGen`.
  - Added a **per-generation type system**: `TYPE_ROSTER` (15 / 17 / 18 by era), `eraOf`, `typesForGen`, `CHART_OVERRIDES`, gen-aware `effGen` / `groupEffectivenessGen`. Existing modern `CHART`/`eff`/`groupEffectiveness` kept.
  - **Historical matchups were verified against PokéAPI `type.past_damage_relations`, not invented** (the earlier web summary had errors). Gen I: Bug↔Poison 2×, Ghost→Psychic 0×, Ice→Fire 1×. Gen II–V: Steel still resists Ghost & Dark (0.5×). Validated with a node harness — 23/23 assertions pass.
- **`wireframes/components.jsx`**: new shared `GenerationCard` (phone list + TV grid, `focused`/`selected`/`size`) and `VersionChip` (header "active generation, tap to switch" pill).
- **`wireframes/version-select.jsx`** (new): `PhoneVersionSelect`, `PhoneVersionSelectLoading` (skeleton), `TVVersionSelect` (D-pad grid).
- **`phone-list.jsx`**: header now binds to the active gen ("NATIONAL · THROUGH GEN I", `/dexEnd`) + a `VersionChip`; list sliced via `dexForGen`.
- **`tv-screens.jsx`**: sidebar "Generation" group replaced by the active-gen card + "≡ MENU TO CHANGE" re-open affordance; Browse subtitle dynamic; Team coverage grid uses `typesForGen` (roster-sized) instead of hardcoded 18.
- **`phone-tools.jsx`**: Type Matchup + Team coverage use the active gen's roster and era chart (`groupEffectivenessGen`, `typesForGen`); subtitle shows "N DEFENDERS · GEN X".
- **`foundations.jsx`**: added `GenerationCard` to the component reference; type-palette title notes the 15/17/18 roster.
- **`app.jsx` + `Pokédex App.html`**: new first canvas section "Root · Version Select"; loads `version-select.jsx`.
- **`CLAUDE.md`**: updated the wireframe file list and added a "Generation context" subsection, flagging that `TypeEffectivenessMatrix` must become **keyed by generation** (still static/hardcoded, still never fetched) in the upcoming code refactor.

### Code refactor — generation context (domain + persistence + phone selector)

Implemented the generation context decided in the wireframe pass.

- **core/domain**:
  - `Generation` model + `Generations` catalog (I–IX: region, cumulative `dexEnd`, versions), mirroring `data.js`.
  - **`TypeEffectivenessMatrix` is now generation-keyed**: modern `chart` baseline + `gen1Overrides` / `gen2to5Overrides` (from PokéAPI `past_damage_relations`) + per-era roster (`typesForGeneration`). `effectiveness` / `attackingEffectiveness` / `defendingWeaknesses` take a `generation` (defaults to latest); types outside the roster resolve to 1×. Still static/hardcoded, never fetched.
  - `GenerationRepository` interface + `GetGenerationsUseCase` / `ObserveSelectedGenerationUseCase` / `SelectGenerationUseCase`.
  - Extended `TypeEffectivenessMatrixTest` with per-gen cases (roster sizes; Gen I Bug↔Poison/Ghost→Psychic/Ice→Fire; Gen II–V Steel resists Ghost/Dark; absent types → 1×). All pass.
- **core/data**: `GenerationRepositoryImpl` backed by **DataStore Preferences** (new `androidx.datastore:datastore-preferences` dep, catalog + module); bound in `RepositoryModule`.
- **core/ui-common**: shared `GenerationCard` + `VersionChip` components and `Generation.accentColor()` (accents reuse type-color tokens, matching the wireframes).
- **app-phone**:
  - **Root generation selector is now the start destination** when no generation is chosen: `AppStartViewModel` gate → `PokedexApp` renders `VersionSelectScreen` (first launch) or the shell. `VersionSelectViewModel` persists the choice and routes into the List.
  - `NavDestination.VersionSelect` route added; `PokedexNavHost` gained `startAtSelector` + the switch-generation flow (List header `VersionChip` → re-open selector → `popUpTo` selector inclusive).
  - `PokemonListViewModel` is generation-scoped: observes the selected generation and loads National Dex `#1..dexEnd` via `flatMapLatest`; exposes `generation` for the header. List header now shows the `VersionChip` + "NATIONAL · THROUGH GEN X" + `/dexEnd`.
  - Updated `PokemonListViewModelTest` for the new constructor (fake `GenerationRepository`).
- Verified: `gradlew :core:domain:test :app-phone:testDebugUnitTest :app-phone:assembleDebug :app-tv:assembleDebug :core:data:assembleDebug` → BUILD SUCCESSFUL; all unit tests pass.

### List pagination + sprite caching (bug fix)

- **Bug fixed: selecting a high generation (e.g. Paldea) showed Kanto.** Root cause was the old eager loader: `getPokemonList(dexEnd, 0)` fired ~`dexEnd` detail calls (1025 for Gen IX) before emitting, and on *any* failure the `catch` fell back to whatever was already cached (the low-numbered Kanto entries). Replaced with real pagination.
  - **Repository**: `getPokemonList(limit, offset): Flow` → **`getPokemonPage(startId, count): List<Pokemon>`** — cache-first by National Dex id window: reads cached rows via new `PokemonDao.getByIds`, fetches only the missing ids (bounded chunks of 12), upserts, returns the window in order. Failures propagate so the UI can show a page error. Removed the unused `pageFlow`/`count` DAO queries.
  - **Domain**: replaced `GetPokemonListUseCase` with `GetPokemonPageUseCase` (clamps the window to the active generation's `dexEnd`; `PAGE_SIZE = 30`).
  - **app-phone**: `PokemonListViewModel` now pages — `UiState<PokemonListData>` where `PokemonListData(items, isAppending, appendError, endReached)`; restarts on generation change, `loadMore()` appends, `retry()` covers first-page error and append error. `PokemonListScreen` drives paging from `LazyListState` (loads the next page within 6 rows of the end), renders skeleton rows while appending and a "Load more" retry on append failure. Generation cap is honored, so Gen IX now pages through all 1025 instead of eager-loading.
- **Sprite caching**: `PokedexApplication` implements `ImageLoaderFactory` — app-wide Coil `ImageLoader` with a persistent 256 MB disk cache (`cacheDir/sprite_cache`), 25% memory cache, and `respectCacheHeaders(false)` (sprites are large and effectively immutable, so they're reused across sessions without refetching). Added `coil.compose` to app-phone.
- Updated `PokemonListViewModelTest` for pagination (first page, first-page error, append, end-of-dex). Updated `CLAUDE.md` repository snippet.
- Note: the dex is still the **cumulative National Dex #1..dexEnd** (the earlier decision), so a Paldea selection legitimately starts at #1 Bulbasaur and pages through to #1025 — the header shows "GEN IX · /1025". If regional dexes (Paldea-only species) are wanted instead, that's a separate change.
- Verified: `gradlew :core:domain:test :core:data:assembleDebug :app-phone:testDebugUnitTest :app-phone:assembleDebug` → BUILD SUCCESSFUL; all unit tests pass.

### Bug fix — paging breaks (stuck at 30) after switching generations

- **Root cause** (in `PokemonListScreen.LoadedList`): the load-more trigger used `remember { derivedStateOf { … data.items.size … } }` with **no keys**, so the lambda closed over the *first* `data`. On the initial generation it kept working (the frozen size stayed exceeded while scrolling), but switching generations conflated the brief `Loading` `StateFlow` emission on a fast cached reload, so `LoadedList` never left composition and the closure kept the *previous* generation's larger `items.size`. `lastVisible >= oldSize - 6` was then never true again → `loadMore` never fired → stuck at the first 30.
- **Fix**: key the derivation on the live `itemCount` + paging flags (`remember(itemCount, canPage) { derivedStateOf { … } }`) so it can't capture stale data; and reset scroll to the top (`LaunchedEffect(resetKey = generation.id) { scrollToItem(0) }`) when the generation changes so a restored deep scroll position doesn't immediately over-page.
- Added a VM regression test (`switching generation resets paging and keeps paging`): pages, switches Gen I → Gen III, asserts the list resets to page 1 (#1 first) and that `loadMore` still appends afterward. Fake generation repo is now a `MutableStateFlow` so switches can be exercised.
- Verified: `gradlew :app-phone:assembleDebug :app-phone:testDebugUnitTest` → BUILD SUCCESSFUL; all tests pass.

### Bug fix (real root cause) — paging stuck at 30 after switching generations

The earlier screen-side fix (keyed `derivedStateOf` + scroll reset) was necessary but not the actual cause. The real defect was in `PokemonListViewModel`: both paging coroutines used `catch (e: Exception) { … }`, which **also catches `CancellationException`**. Switching generations calls `pagingJob?.cancel()` to stop the previous generation's in-flight load; that cancellation threw `CancellationException`, which the `catch` swallowed and turned into a corrupted state write — `appendError = true` (or `Error`) carrying the *previous* generation's `data`, and a stale `nextStartId`. `appendError`/`isAppending` make `canPage` false, so the list never requested another page → permanently stuck at the first 30.

- **Fix**: in both `loadFirstPage` and `loadMore`, rethrow `CancellationException` and `ensureActive()` before committing, so a cancelled load can never write state or advance `nextStartId`.
- **Test**: added `switching while a page load is in flight does not corrupt paging` — starts an append, switches generation mid-flight (cancelling it), and asserts no `appendError`/stuck `isAppending`, the list resets to page 1, and `loadMore` still works. `FakeRepository` gained an optional `delayMillis` so an in-flight load can be held across the switch.
- Verified: `gradlew :app-phone:testDebugUnitTest :app-phone:assembleDebug` → BUILD SUCCESSFUL; all tests pass.

### Logging standards + instrumentation

The paging-after-switch bug is still reproducing despite two attempted fixes, so added proper logging to diagnose with real evidence instead of guessing.

- **`CLAUDE.md`**: new **Logging** section — use **Timber** (no `android.util.Log`/`println`); plant `DebugTree` only in debug (`BuildConfig.DEBUG`); automatic class-name tags; level guidance (v/d/i/w/e); format-args not concatenation; log at boundaries (repo, ViewModel, navigation/selection); keep `core/domain` pure (no logging); greppable structured messages; no secrets/PII.
- **Dependency**: added Timber (`com.jakewharton.timber:timber:5.0.1`) to the catalog + `app-phone` and `core:data`. Enabled `buildConfig` for `app-phone`; `PokedexApplication.onCreate` plants `Timber.DebugTree()` in debug.
- **Instrumented** the paging path end-to-end:
  - `PokemonListViewModel`: generation-change → restart, `loadFirstPage`/`loadMore` fetch + result (gen id, startId, page size, total, `nextStartId`, `endReached`), every skip reason (`isAppending`/`endReached`/non-Success), cancellations, and errors.
  - `PokemonRepositoryImpl.getPokemonPage`: window, cache-hit vs missing counts, API fetch size, returned size.
  - `GenerationRepositoryImpl`: `selectGeneration` writes + `selectedGenerationId` emissions.
  - `PokemonListScreen.LoadedList`: `resetKey` scroll-to-top and each `loadMore` trigger (with `shouldLoadMore`/`itemCount`/`canPage`/`lastVisible`).
  - `VersionSelectViewModel.select` and `AppStartViewModel` start-gate transitions.
- The cancellation-safety fix (rethrow `CancellationException` + `ensureActive`) and the screen keyed-`derivedStateOf` fix remain in place.
- Verified: `gradlew :core:data:assembleDebug :app-phone:testDebugUnitTest :app-phone:assembleDebug` → BUILD SUCCESSFUL; all tests pass.

### Pokédex List — search bar (wired end to end)

Completed the in-progress search feature. The data/domain/ViewModel scaffolding was already
present (uncommitted `searchPokemon` on the repository + impl, `SearchPokemonUseCase`, the
`SearchUiState` sealed interface, and the `query`/`searchState` flows on `PokemonListViewModel`).
This turn wired the UI and closed a retry gap.

- **`PokemonListScreen`**: replaced the static placeholder `SearchBar` with a real
  `BasicTextField` (search-icon leading, hint when empty, dexEnd counter when empty, clear `×`
  button when non-empty; IME action = Search, Fire-colored cursor). `PokemonListContent` now
  collects `query`/`searchState` and renders by search state: `Idle` shows the paged dex
  underneath as before; `Loading` reuses the skeleton list; `Results` renders a `LazyColumn` of
  `ListRow`s; `Empty` and `Error` get dedicated centered states (the latter with a Retry button).
- **Retry correctness**: re-running a failed search by re-setting the same query string is a
  no-op (`StateFlow` dedupes equal values), so added a `_searchRetry` counter folded into the
  search `combine` (now a `Triple`, so `distinctUntilChanged` sees the bump) and a
  `retrySearch()` the error state calls.
- **Strings**: added `list_search_clear`, `list_search_empty_title`, `list_search_empty_body`,
  `list_search_error`.
- **Tests**: updated `FakeRepository` for the new `searchPokemon` method and the ViewModel's
  third constructor arg (`SearchPokemonUseCase`); added cases for blank→Idle, name→Results,
  out-of-generation dex number→Empty, clear→Idle, and failure→Error→retry→Results (toggleable
  `failSearch` on the fake).
- Verified: `gradlew :app-phone:compileDebugKotlin` and
  `gradlew :app-phone:testDebugUnitTest` → BUILD SUCCESSFUL; all tests pass.

## 2026-06-08

### Phone screen #2 — Pokémon Detail (four tabs, fully data-backed)

Built the Pokémon Detail screen from `wireframes/phone-detail.jsx` (Direction B "Console Card":
HUD header card + segmented Stats/Moves/About/Evo tabs, accent driven by the primary type).
The user chose to back **all four tabs with real data**, so this was a full vertical slice
through every layer rather than just the two tabs (Stats/About) the existing `Pokemon` model
already supported.

- **core/domain**: new `PokemonDetail` aggregate (composes base `Pokemon` + `genus`,
  `flavorText`, `abilities`, `captureRate`, `moves`, `evolution`); `MoveInfo` + `MoveCategory`
  (PHYSICAL/SPECIAL/STATUS); `EvolutionStage` (id/name/sprite/types/condition). The base
  `Pokemon` model is unchanged — list/team/search keep reusing it. Added
  `PokemonRepository.getPokemonDetailFull(id)` and `GetPokemonDetailFullUseCase`.
- **core/data — DTOs + endpoints**: extended `PokemonDetailDto` with `abilities`/`moves`/
  `species` (nullable/defaulted, so the list-paging path that reuses `/pokemon/{id}` ignores
  them at no cost). Added `MoveDto`, `PokemonSpeciesDto` (genera/flavor/capture_rate/
  evolution_chain), `EvolutionChainDto`/`ChainLinkDto`/`EvolutionDetailDto`, and a name-less
  `ApiResourceDto` for the species→evolution_chain link. New `PokeApiService` endpoints:
  `getPokemonSpecies`, `getEvolutionChain`, `getMove(name)`.
- **core/data — cache**: new flat `PokemonDetailEntity` + `PokemonDetailDao`; moves/evolution
  stored as Gson-serialized JSON String columns (kept flat per the entity rule). `PokedexDatabase`
  bumped **v1 → v2** (destructive fallback already configured, so no manual migration); DAO
  provided in `DataModule`.
- **core/data — repository**: `getPokemonDetailFull` is cache-first (rebuilds the aggregate from
  the cached detail row + cached base; on miss does ONE `/pokemon` fetch for base+refs, then
  species, level-up moves, and the evolution line). Moves: filter to `level-up`, min level per
  move, sort by level then name, cap 24, fetch each `/move` concurrently (failures drop that
  move). Evolution: pre-order flatten of the chain (cap 8), each stage's sprite/types via the
  cache-first base lookup, condition label derived from `evolution_details`
  (Lv. N / item / held item / Friendship / Trade / trigger). Injected `Gson` + `PokemonDetailDao`.
- **app-phone**: `PokemonDetailViewModel` (`@HiltViewModel`, id from `SavedStateHandle`, exposes
  `StateFlow<UiState<PokemonDetail>>`, `retry()`); `PokemonDetailScreen` with the HUD header,
  segmented tabs (selection survives config change via `rememberSaveable`), and all four panes —
  Stats (shared `StatBar` + total), Moves (type badge + category color + power/acc/pp), About
  (flavor, height/weight/category/abilities grid, catch-rate bar), Evo (horizontally-scrolling
  nodes + chevrons + condition; "does not evolve" fallback). Loading = skeleton header+tabs+rows;
  Error = retry state. Wired into `PokedexNavHost` (replaced the Detail placeholder; back via
  `popBackStack`). Move-category colors taken from the wireframe `catColor`. Height/weight
  converted from PokéAPI tenths. The wireframe's favorite-star control was omitted (no favorites
  feature to back it); the back button is functional.
- All user-facing strings added to `strings.xml`.
- **Tests**: `PokemonDetailViewModelTest` (loads detail for the saved-state id; failure→Error→
  retry→Success). Updated the list test's `FakeRepository` for the new interface method.
- Verified: `gradlew :core:domain:test :app-phone:testDebugUnitTest :app-phone:assembleDebug
  :app-tv:assembleDebug` → BUILD SUCCESSFUL; all unit tests pass.

## 2026-06-08 — Type Matchup calculator (phone, screen #4)

Implemented the "Types" tab (the Type Matchup Calculator), replacing its placeholder.

- **core/domain**: new `EffectivenessGroup` enum (SUPER ×2 / NORMAL ×1 / NOT_VERY ×½ /
  NO_EFFECT ×0, in attacker-favorable display order) with `forMultiplier()`. New
  `GroupTypeEffectivenessUseCase` — pure, static grouping of a generation's roster by how an
  attacking type fares against each defender, built on the existing `TypeEffectivenessMatrix`
  (no network). Always returns all four group keys in order.
- **app-phone `ui/typecalc/`**: `TypeMatchupViewModel` combines `ObserveSelectedGenerationUseCase`
  with a selected-attacker StateFlow, exposing `StateFlow<UiState<TypeMatchupData>>` (Loading until
  the generation resolves, then Success). Attacker defaults to Fire (per wireframe), falling back
  within the roster. `TypeMatchupScreen` renders the header, attacker hero card · "vs" · static
  "All Types" defender card, a tappable roster strip (FlowRow of TypeBadges; selected = filled,
  others = soft) to pick the attacker, and the four grouped sections with colored markers,
  multiplier, count, and badges — derived from `wireframes/phone-tools.jsx`. Loading renders
  skeletons; Error branch included for completeness.
- Wired `TypeMatchupScreen()` into the `TypeCalc` tab in `PokedexNavHost`.
- All user-facing strings added to `strings.xml` (`typecalc_*`).
- **Tests**: `TypeMatchupViewModelTest` — default Fire attacker groups, attacker re-selection
  recomputes groups, Gen I roster excludes Dark/Steel/Fairy.
- Verified: `gradlew :core:domain:test :app-phone:testDebugUnitTest :app-phone:assembleDebug`
  → BUILD SUCCESSFUL; all unit tests pass.

## 2026-06-08 — Type Matchup: defender selection + defensive mode (phone)

Extended the Type Matchup calculator with a second, defensive mode driven by selecting defender
type(s).

- **core/domain**: new `DefenseBucket` enum (×4 QUAD / ×2 DOUBLE / ×1 NEUTRAL / ×½ HALF /
  ×¼ QUARTER / ×0 IMMUNE, worst-to-best for the defender) with `forMultiplier()` — covers the
  stacked multipliers a dual-type combo produces. New `GroupDefenseEffectivenessUseCase` groups
  every attacking type in the generation by how hard it hits a 1–2 type defender combo, built on
  `TypeEffectivenessMatrix.defendingWeaknesses` (still static, no network).
- **TypeMatchupViewModel**: added a `defenderSelection` StateFlow (up to 2 distinct types — a valid
  mono/dual combo; tapping a selected type removes it, a third pick evicts the oldest). `state`
  now combines generation + attacker + defenders and exposes both `attackGroups` and
  `defenseGroups` plus the chosen `defenders` and a `defending` flag. Defenders are sanitized
  against the active generation's roster (e.g. Fairy drops in Gen I). New `toggleDefender` /
  `clearDefenders`.
- **TypeMatchupScreen**: the defender card now renders the chosen combo (badges) or "All Types";
  added a "Choose defending types (up to 2)" chip strip with a Clear action and a result-mode
  label. When a defender combo is selected the result switches to the six defensive buckets
  (Double Weak … Immune, weakness = warm, resist = green, immune = purple); with no defender it
  stays the offensive grouping. `GroupSection` was generalized to serve both modes.
- Added `typecalc_pick_defender`, `typecalc_clear`, `typecalc_mode_*`, and `typecalc_def_*`
  strings.
- **Tests**: `TypeMatchupViewModelTest` gains offensive-mode default, Fire/Flying combo stacking
  into ×4 (Rock) and ×0 (Ground), and the 2-defender cap/eviction/clear behavior.
- Verified: `gradlew :core:domain:test :app-phone:testDebugUnitTest :app-phone:assembleDebug`
  → BUILD SUCCESSFUL; all unit tests pass.

## 2026-06-08 — Type Matchup: dual attacker types (phone)

Made the attacking side symmetric with the defender side — you can now pick up to 2 attacking types.

- **GroupTypeEffectivenessUseCase**: signature changed `attacker: Type` → `attackers: List<Type>`.
  With two types it models offensive coverage, bucketing each defender by the *best* (max)
  multiplier across the selected move-types (moves are independent, so you'd use the most
  effective one). Single-type behavior is unchanged.
- **TypeMatchupViewModel**: `attackerSelection` is now a `List<Type>` seeded with the default
  (Fire) so it's a real, buildable selection; `selectAttacker` → `toggleAttacker` (same up-to-2
  cap + oldest-eviction as the defender). Emptying it resolves back to the default. `TypeMatchupData.attacker`
  → `attackers: List<Type>`.
- **TypeMatchupScreen**: the attacker/defender hero cards now share one `TypeHeroCard` (single
  type → name + badge; combo → badges; empty → "All Types" placeholder). The attacker chip strip
  toggles up to 2; the offensive result-mode label shows the attacker combo name.
- String `typecalc_pick_attacker` → "Choose attacking types (up to 2)".
- **Tests**: replaced the single-attacker assertion with `attackers` list checks; added
  default-fallback-on-empty and Fire+Ground coverage-of-Rock (best-multiplier) cases.
- Verified: `gradlew :core:domain:test :app-phone:testDebugUnitTest :app-phone:assembleDebug`
  → BUILD SUCCESSFUL; all unit tests pass.

## 2026-06-08 — Type Matchup: revert dual attacker, relabel "Attacking move" (phone)

Walked back the previous dual-attacker change per the developer — the attacker is a single type
again; only the defender supports up-to-2 (a valid mono/dual combo).

- **GroupTypeEffectivenessUseCase**: signature back to `attacker: Type`.
- **TypeMatchupViewModel**: `attackerSelection` back to a single `Type?` with `selectAttacker`;
  `TypeMatchupData.attackers` → `attacker: Type`. Defender list logic unchanged.
- **TypeMatchupScreen**: attacker chip strip is single-select again; the shared `TypeHeroCard`
  receives the attacker as a one-element list.
- **Language**: relabeled "Attacker" → "Attacking move" (`typecalc_attacker`), picker →
  "Choose attacking move type", offensive mode label → "%1$s move · vs every defender" — the
  wording the developer found clearer.
- **Tests**: restored single-attacker default + `selectAttacker` recompute cases.
- Verified: `gradlew :core:domain:test :app-phone:testDebugUnitTest :app-phone:assembleDebug`
  → BUILD SUCCESSFUL; all unit tests pass.

## 2026-06-08 — Type Matchup: drop attacking move, defensive-only (phone + wireframe)

Per the developer, the attacking-move picker was redundant — the "Every attacker" defensive list
already maps every attacking type. Removed it; the screen is now a pure defensive calculator.

- **Removed** `EffectivenessGroup` and `GroupTypeEffectivenessUseCase` (the offensive grouping —
  now dead). `DefenseBucket` doc updated to no longer reference them.
- **TypeMatchupViewModel**: dropped the attacker entirely (no `attackerSelection`/`selectAttacker`,
  no `GroupTypeEffectivenessUseCase` dep). `state` combines generation + defenders only;
  `TypeMatchupData` now just `{ generation, roster, defenders, defenseGroups }`. Empty selection =
  prompt state.
- **TypeMatchupScreen**: removed the attacker hero card, the "vs", the attacking-move chip strip,
  and the offensive result branch. Now: a single defending-type hero card (or "Pick a type"
  placeholder), the defender chip strip (up to 2) with Clear, and the six defensive buckets. Added
  an empty-state prompt shown until a type is picked.
- **strings.xml**: removed all attacker/offense strings (`typecalc_attacker`, `typecalc_vs`,
  `typecalc_all_types*`, `typecalc_pick_attacker`, `typecalc_mode_offense`, `typecalc_group_*`,
  `typecalc_mult_*`). Subtitle → "DEFENDING · %1$d ATTACKERS · GEN %2$s"; added
  `typecalc_defender_empty_*` and `typecalc_prompt*`.
- **Wireframe**: `data.js` gains `groupDefenseGen(defs, gen)` (buckets every attacker by the stacked
  ×4…×0 multiplier on a 1–2 type combo) + export. `phone-tools.jsx` `PhoneMatchup` rewritten to the
  defensive layout (defending-type hero + chip strip + six buckets), dropping the attacker picker.
  Note: the rendered `Pokédex App.html` is a Claude Design output and is now stale vs the `.jsx`
  source (the source of truth); it would need re-rendering in Claude Design.
- **Tests**: removed the offensive cases; kept/added the empty-start, Gen I roster, Fire/Flying
  ×4-Rock/×0-Ground stacking, and 2-defender cap/eviction/clear cases.
- Verified: `gradlew :core:domain:test :app-phone:testDebugUnitTest :app-phone:assembleDebug`
  → BUILD SUCCESSFUL; all unit tests pass.

## TeamSlotGrid.kt
Added stateless TeamSlotGrid composable (app-phone ui/team): 2x3 grid of six team slots. Filled slots show accent gradient, dex #, sprite, name, type stripes, and a remove affordance; empty slots show a dashed add tile. Tapping a slot calls onSlotClick(index); remove calls onRemove(index). Uses strings team_slot_add/team_slot_remove and ui-common tokens/PokemonSprite. Previewable.

## TeamCoveragePanel.kt
Added stateless TeamCoveragePanel composable (app-phone ui/team) rendering a TeamCoverage. Empty team shows team_coverage_empty. Defensive section: 6-col roster grid highlighting types with defensiveWeaknesses>0 (count badge for shared weaknesses), header + team_coverage_weak_points from weakPointCount, shared/covered legend. Offensive section: offensiveGaps as soft TypeBadges + hint, or team_coverage_offensive_none when empty. All values read off the passed TeamCoverage; no computation or hardcoded type data.

## TeamPickerSheet.kt
Added stateless TeamPickerSheet (app-phone ui/team): a ModalBottomSheet driven by TeamPickerUiState. Renders nothing when Closed; when Open shows title (team_picker_title_add/_replace by replacing, slot+1), a search field bound to query (onQueryChange, clear button), an optional Remove action (team_slot_remove -> onRemove(slot)) when replacing, and the PickerResults body: Idle->team_picker_prompt, Loading->spinner, Empty->team_picker_empty(query), Error->team_picker_error, Results->tappable rows calling onSelect(Pokemon). Reuses list screen SearchBar/ListRow idiom. onDismiss wired to sheet dismiss.

## TeamScreen.kt
Added TeamScreen route composable (app-phone ui/team) wiring the pieces to TeamViewModel via hiltViewModel. Collects state + picker with collectAsStateWithLifecycle. Scaffold with TopAppBar (team_title + team_count from filledCount) and Extended FAB (team_add_fab -> openAddPicker). Loading->spinner, Error->message, Success->scrollable TeamSlotGrid + TeamCoveragePanel. Renders TeamPickerSheet (self-gates on Closed). Wired: slot tap->openPicker, slot remove->removeSlot, query->onPickerQueryChange, select->selectPokemon, picker remove->removeSlot, dismiss->closePicker. Completes the Team Builder UI.

## Build fix
Built :app-phone:assembleDebug. Fixed a missing import in TeamScreen.kt (androidx.compose.runtime.getValue) that broke the collectAsStateWithLifecycle by delegates, which cascaded into UiState.Success cast/type-argument errors. Added the import; build now passes (BUILD SUCCESSFUL).

## Remove Team FAB
Removed the extraneous "Add Pokemon" ExtendedFloatingActionButton from TeamScreen (adding to a slot is done by tapping a slot tile -> openPicker). Dropped now-unused imports. openAddPicker() and string team_add_fab are now unused but left in place. Build passes.

## Cleanup orphaned FAB refs
Deleted unused TeamViewModel.openAddPicker() and the orphaned team_add_fab string from strings.xml (both left over from the removed Add Pokemon FAB). Build passes.

## Wireframes: Items & Moves dictionaries
Confirmed PokeAPI v2 supports both views (move endpoint: power/pp/accuracy/type/damage_class/generation; item endpoint: cost/category/effect/sprites — noted items lack a reliable introduced-in-gen list, so item gen-scoping is best-effort while moves scope strictly). Added two new phone dictionary mockups to /wireframes, mirroring the Pokédex List (searchable list + filter chips + tappable rows → detail, with loading-skeleton and error states):
- data.js: added ITEMS, ITEM_CATEGORIES, MOVES sample datasets + itemById/moveById and movesForGen/itemsForGen helpers; exported on window.PDX.
- components.jsx: BottomNav now reads Pokédex · Items · Moves · Team · Matchup (dropped "Saved"); added Ic.bag and Ic.move icons.
- New phone-items.jsx (PhoneItems/Loading/Error + ItemDetail) and phone-moves.jsx (PhoneMoves/Loading/Error + MoveDetail).
- Registered both as new DCSections in app.jsx and added their <script> tags to "Pokédex App.html".

## 2026-06-08 — Items dictionary (phone: list + detail)

Implemented the Items dictionary screens from `wireframes/phone-items.jsx` (list with
search + category chips + tappable rows → item detail; all three UiState cases). Two
decisions confirmed with the developer: **national/best-effort generation scoping** (the
item dictionary is the full national set — PokéAPI has no reliable introduced-in-gen list —
with the `VersionChip` kept in the header for consistency/navigation) and **Items only**
this pass (Moves remains future work).

- **core/domain**: `Item` model + `ItemCategory` enum. Categories are coarse display
  buckets (Poké Balls / Healing / Medicine / Evolution / Held / Mega Stones / Key Items /
  Other) that roll up PokéAPI's ~50 granular `item-category` names via `fromApiName`
  (unknown → `OTHER`); `slug`-based persistence round-trips through `fromSlug`.
  `ItemRepository` interface + `GetItemPageUseCase` (PAGE_SIZE 30) / `SearchItemsUseCase` /
  `GetItemDetailUseCase`.
- **core/data**: `ItemDtos` (`ItemListResponseDto`, `ItemDetailDto` + effect/flavor/sprite
  DTOs); `PokeApiService.getItemList`/`getItem`. Flat `ItemEntity` (category stored as the
  bucket slug) + `ItemDao`; `PokedexDatabase` bumped **v2 → v3** (destructive fallback, no
  manual migration), entity/DAO registered, DAO provided in `DataModule`. `ItemMappers`
  (DTO→entity picks English short_effect/flavor, whitespace-normalized; reuses
  `String.toTitle`). `ItemRepositoryImpl`: cache-first paging keyed off the `/item` list
  endpoint's id window (bounded detail-fetch chunks of 12), search by id or name against a
  once-fetched in-memory name index, cache-first detail. Bound in `RepositoryModule`.
- **app-phone `ui/items/`**: `ItemsListViewModel` (offset paging mirroring the dex list —
  cancellation-safe, append/retry, `loadMore`; client-side category filter via
  `ItemsListData.visible`; debounced search with retry-counter) and `ItemsListScreen`
  (header `VersionChip`/title/subtitle/count + search field + horizontally-scrolling
  category chips + rows with gold `ItemIcon`/`CostTag`; Loading skeletons, Error+Retry,
  search Empty/Error states, and a category-empty hint). `ItemDetailViewModel`
  (id from `SavedStateHandle`) + `ItemDetailScreen` (back bar, gold gradient header card
  with category eyebrow + Buy/cost, Effect section, Description card; Loading skeleton +
  Error/Retry). Gold accent `#C9A24A` (`ItemAccent`) lives in the items package — it is not
  a shared/type color. The wireframe's filter FAB and favorite star were omitted (the
  category chips already filter; no favorites feature).
- **nav**: added `NavDestination.Tab.Items` (Backpack icon) + `NavDestination.ItemDetail`
  route; wired both into `PokedexNavHost` and expanded the bottom bar to four tabs
  (Dex · Items · Team · Types).
- All user-facing strings added to `strings.xml` (`items_*`, `item_*`, `nav_items`).
- **Tests**: `ItemsListViewModelTest` (first page / first-page error / append / end-of-dict /
  category filter / search Idle·Results·Empty·Error→retry) and `ItemDetailViewModelTest`
  (load by saved-state id; failure→Error→retry→Success), with fake item/generation repos.
- Verified: `gradlew :core:domain:test :core:data:assembleDebug :app-phone:testDebugUnitTest
  :app-phone:assembleDebug :app-tv:assembleDebug` → BUILD SUCCESSFUL; all unit tests pass.

## 2026-06-08

### Moves dictionary (phone) — Pokédex · Items · **Moves** · Team · Matchup

Implemented the generation-scoped Moves dictionary per `wireframes/phone-moves.jsx`, mirroring
the Items feature. Key difference vs. Items: **moves are strictly generation-scoped** (Items are
national). The scoped id set is derived from PokéAPI `/generation/{id}` move lists (union of
generations `1..selected`), giving an exact "through gen X" set + known total without per-move
detail fetches; pages then resolve cache-first like Items.

- **core/domain**: `Move` model (reuses `MoveCategory`; `type` nullable for non-battle types),
  `MoveRepository` (`getMovePage(genId, offset, count)` / `searchMoves(genId, query)` /
  `getMoveDetail(id)`), `MoveUseCases` (`GetMovePageUseCase` PAGE_SIZE=30, `SearchMovesUseCase`,
  `GetMoveDetailUseCase`).
- **core/data**: `MoveDtos` (`GenerationDto`, `MoveDetailDto`, `MoveEffectEntryDto`), `MoveEntity`
  (flat; type-enum-name + damage-class slug), `MoveDao`, `MoveMappers` (gen-slug→id, English
  short_effect, whitespace clean), `MoveRepositoryImpl` (mutex-guarded per-gen ref memoization,
  scoped-window paging cache-first, gen-scoped name/id search). Added `getMoveDetail(id)` +
  `getGeneration(id)` to `PokeApiService`; registered `MoveEntity` in `PokedexDatabase`
  (v3→v4, destructive); `MoveDao` provider in `DataModule`; `MoveRepository` bind in `RepositoryModule`.
- **app-phone** (`ui/moves/`): `MovesListScreen` (header + VersionChip, search bar, damage-class
  chips All/Physical/Special/Status, paged list with skeleton/append/error, MoveRow = name +
  TypeBadge + class label + mono power/acc·pp), `MovesListViewModel` (reloads the dictionary on
  generation change — the behavioral delta from Items), `MovesListData`/`MovesSearchUiState`,
  `MoveComponents` (dragon screen accent, damage-class colors), `MoveDetailScreen`
  (type-colored gradient header, Power/Acc/PP stat tiles, Effect) + `MoveDetailViewModel`.
  Added `Tab.Moves` + `MoveDetail` route to `NavDestination`/`PokedexNavHost`; strings in
  `strings.xml`. Following the shipped Items pattern, used inline class chips instead of the
  wireframe's decorative "Filter" FAB.
- **Tests**: `MovesListViewModelTest` (first page / error / append / end-of-dict / **generation
  change reloads** / class filter / search Idle·Results·Empty·Error→retry) and
  `MoveDetailViewModelTest` (load by saved-state id; failure→Error→retry→Success), with fake
  move/generation repos. 12 tests, all pass.
- Verified: `gradlew :app-phone:assembleDebug :app-phone:testDebugUnitTest
  :core:data:compileDebugKotlin` → BUILD SUCCESSFUL.

## TV wireframes — parity with the phone (design pass)
- **wireframes/tv-screens.jsx**: rewrote to bring the TV (leanback) target to feature parity with
  the phone. Added the missing screens, each drawn in the TV idiom (16:9 `TVFrame`, explicit
  `.pdx-focused` D-pad ring, footer hint strips):
  - **Nav rail** (`TVNavRail`): a persistent left icon+label rail (Pokédex · Items · Moves · Team ·
    Matchup) that replaces the phone bottom nav as the TV's primary navigation. Present on every
    top-level screen; detail screens stay rail-less with a BACK affordance.
  - **Items** dictionary: `TVItems` (3-col card grid + category sidebar), `TVItemsLoading`,
    `TVItemsError`, `TVItemDetail` (split hero + Effect/Description). Gold accent, `itemsForGen`.
  - **Moves** dictionary: `TVMoves` (wide rows + damage-class sidebar), `TVMovesLoading`,
    `TVMovesError`, `TVMoveDetail` (split hero + Power/Acc/PP tiles + Effect). Dragon accent,
    `movesForGen`.
  - **Type Matchup** (`TVMatchup`): defender hero + defending-type picker on the left, grouped
    every-attacker results (×4…×0) on the right; uses `groupDefenseGen`.
  - **Browse error** (`TVBrowseError`) to round out the list states.
  - Refactored shared sidebar pieces: `TVGenBlock`, `TVFilterSection` (type/category/class),
    `TVContentHeader`, `TVSearchPill`, `TVHints`, `TVError`. Updated `TVBrowse`/`TVTeam` to sit
    beside the nav rail.
- **wireframes/app.jsx**: registered all new TV artboards under the "TV · Leanback" section.
- **CLAUDE.md**: updated the `tv-screens.jsx` wireframe description to reflect the expanded TV
  coverage and the nav-rail navigation model.
- Note: in-progress `app-tv` Kotlin scaffolding (build.gradle/manifest/Hilt app/nav/version-select)
  was started earlier this session; implementation of the TV Compose screens against these updated
  wireframes is the next step.

## TV parity build — shared ViewModels + all leanback screens

Brought the Android TV target to full feature parity with the phone, implementing every screen in
the `tv-screens.jsx` wireframes against real ViewModels.

- **New module `core/ui-state`** (Compose-free android-library, Hilt + lifecycle-viewmodel): lifted
  all six screen ViewModels and their UiState/data classes out of `app-phone` so phone + TV share one
  source of truth — `PokemonListViewModel`, `PokemonDetailViewModel`, `TeamViewModel`,
  `TypeMatchupViewModel`, `ItemsListViewModel`/`ItemDetailViewModel`,
  `MovesListViewModel`/`MoveDetailViewModel` (+ `SearchUiState`, `*ListData`, `*SearchUiState`,
  `TeamData`/`TeamPickerUiState`/`PickerResults`, `TypeMatchupData`). Each detail VM now owns its own
  `ARG_ID` nav-key constant so it no longer depends on the phone `NavDestination`. Registered in
  `settings.gradle.kts`; added `lifecycle-viewmodel-ktx`/`-savedstate` aliases to the version catalog.
- **app-phone**: deleted the moved files, added the `:core:ui-state` dependency, and updated screen
  imports to the shared package. The seven moved ViewModel unit tests now live in
  `core/ui-state/src/test` and pass.
- **app-tv shared chrome** (`tv/ui/common/TvChrome.kt`): `TvNavRail` (5-dest left rail),
  `TvScreenScaffold`, `TvSidebar`/`TvGenBlock`/`TvFilterSection`, `TvContentHeader`, `TvSearchField`
  (real D-pad text entry), `TvHints`, `TvBackBar`, `TvErrorState`. All D-pad focusable via the
  existing `tvFocusRing`.
- **app-tv screens** (consume the shared VMs, all three UiState cases + skeleton loading):
  - Browse grid + Pokémon detail (split stats panel + Moves/About/Evolution tabs).
  - Items list + item detail; Moves list + move detail (with TV-local accent/label helpers).
  - Team builder (slots row, defensive-coverage grid, offensive-gaps panel, search picker overlay).
  - Type Matchup (defender hero + picker grid + grouped ×4…×0 results).
- **app-tv nav**: expanded `TvDestination` (Items/ItemDetail/Moves/MoveDetail/Matchup) and rewrote
  `PokedexTvApp` so the nav rail switches top-level destinations (saveState/restoreState) and detail
  screens are full-screen-with-back. Added all required `strings.xml` entries. Fixed missing
  `getValue` import in `TvFocus.kt`.
- **CLAUDE.md**: documented the new `core/ui-state` module and the shared-ViewModel architecture.
- Build: `:app-tv:assembleDebug` + `:app-phone:assembleDebug` succeed; `core:ui-state` unit tests pass.

## Team Builder slot grid — sprites too small (2026-06-09)
- **Bug**: Team Builder slots rendered in a 2-column grid, making each square slot oversized so the
  fixed 48dp sprite looked tiny and didn't fill the box.
- **Fix**: `TeamSlotGrid.kt` `COLUMNS` changed 2 → 3 to match `wireframes/phone-tools.jsx`
  (`gridTemplateColumns: 'repeat(3, 1fr)'`).

## TV Team Builder — picker dialog didn't grab focus (2026-06-09)
- **Bug**: Opening the "Add to slot" picker overlay on TV left D-pad focus on the slot grid behind
  the dialog, so the search field couldn't be reached.
- **Fix**: `TvTeamScreen.PickerOverlay` now holds a `FocusRequester` attached to `TvSearchField`
  and calls `requestFocus()` in a `LaunchedEffect(open.slot)` when the overlay opens.

## TV Type Matchup — removed left sidebar (2026-06-09)
- **Request**: the left sidebar (explanation paragraph + generation block) made the screen margins
  awkward.
- **Change**: `TvMatchupScreen` no longer renders a `TvSidebar`; the content now spans the full
  width under the nav rail. Dropped the unused `onSwitchGeneration` param and its call-site wiring
  in `PokedexTvApp` (generation is still switchable from the other screens). Removed now-unused
  `TvSidebar`/`TvGenBlock` imports. The `matchup_sidebar_title`/`matchup_sidebar_body` strings are
  now unused but left in place.

## TV Type Matchup — bottom result groups unreachable/cut off (2026-06-09)
- **Bug**: the grouped results column overflowed the viewport (e.g. "Double Resists" cut off) and
  couldn't be scrolled: the outer `Row` used `fillMaxSize()` (pushing the hint bar off-screen) and
  nothing inside the `verticalScroll` results column was focusable, so the D-pad couldn't drive the
  scroll.
- **Fix** (`TvMatchupScreen`): wrapped the body in a `Column(fillMaxSize)` and gave the content
  `Row` `weight(1f)` so it's bounded and the hint bar stays visible. Each `DefenseBucket` group is
  now `focusable` with a `tvFocusRing`, so D-pad navigation moves group-to-group and `verticalScroll`
  brings the focused group into view — the last bucket is now reachable.

## 2026-06-09

### Wireframe canvas refactor (app.jsx)
- Moved **Foundations** (design system / shared tokens & components) to the top of the design canvas, above all screens.
- Reorganized the canvas so each **view** pairs its Phone and TV artboards in the same section row, instead of grouping all phone screens first and a single "TV · Leanback" section last. New sections: Foundations · Root Version Select · Pokédex List/Browse · Pokémon Detail · Items Dictionary · Moves Dictionary · Team & Matchup — each containing both targets' states side by side. Artboard labels prefixed with "Phone —" / "TV —" for clarity.
- No component sources changed; this is purely the `app.jsx` assembly. No `.design-canvas.state.json` sidecar exists, so no persisted order/label state needed migration.

## GitHub Actions CI
- Added `.github/workflows/ci.yml`: runs on push/PR to `master`. Sets up JDK 17 (Temurin) and the
  Android SDK, runs `./gradlew test`, assembles debug APKs for both app-phone and app-tv, and
  uploads test reports + debug APKs as artifacts. Uses `gradle/actions/setup-gradle` for build
  caching and `concurrency` to cancel superseded runs.
