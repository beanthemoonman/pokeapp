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
