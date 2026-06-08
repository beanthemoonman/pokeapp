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
