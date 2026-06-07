# Pokédex — Claude Code Project Guide

## Notes from Management (The Developer)

 - End your turn by saying Bada Bing!
 - Append what you did during your turn to the end of the file claude_changelog.md.
 - If anything you did means that now something in claude.md is incorrect, please update it.

## IMPORTANT! DOCUMENTATION FOR API

All data should come from v2 of the PokeApi portal. Documentation for which may be found here, 
https://pokeapi.co/docs/v2

## Project Overview

A two-target Android Pokédex app: a standard phone app and an Android TV app. Both targets share a
common data layer, domain layer, and UI component library. The app is built entirely with Jetpack
Compose and follows a unidirectional data flow architecture (UDF) using ViewModels and StateFlow.

The starting point is a default Android Studio application template. Most of the generated
boilerplate can be overwritten. Retain the Gradle wrapper, `.gitignore`, and root `build.gradle`
structure, but restructure into the multi-module layout described below.

---

## Wireframes

Wireframes from Claude Design are in `/wireframes`. Before implementing any screen, read the
corresponding wireframe. The wireframes are the source of truth for layout, component structure,
and named design tokens. Do not invent layouts or component names — derive them from the wireframes.

Wireframe files (flat `.jsx`/`.js` sources from Claude Design, plus a rendered `.html`):
- `wireframes/phone-list.jsx`, `wireframes/phone-detail.jsx`, `wireframes/phone-tools.jsx` — phone screens
- `wireframes/tv-screens.jsx` — TV screens
- `wireframes/components.jsx` — shared component specs (TypeBadge, StatBar, Sprite, nav chrome)
- `wireframes/foundations.jsx` — design-system reference (surface tokens, typography, type palette)
- `wireframes/data.js` — type color tokens, stat metadata, type-effectiveness CHART, sample data

---

## Module Structure

```
pokedex/
├── app-phone/                          # Phone APK target
├── app-tv/                             # TV APK target
└── core/
    ├── data/                           # API, Room DB, repositories
    ├── domain/                         # Use cases, domain models, type matrix
    └── ui-common/                      # Shared Compose components, theme
```

When adding new functionality, determine which module it belongs to before writing any code.
Business logic lives in `core/domain`. Network/persistence lives in `core/data`. Shared Compose
components live in `core/ui-common`. Screen-level composables live in the appropriate app module.

---

## Target Configuration

### app-phone
- `minSdk 30`, `targetSdk 36`, `compileSdk 36`
- Launcher activity: standard `MAIN` / `LAUNCHER` intent filter
- Navigation: Jetpack NavHost with bottom navigation bar (List, Team Builder, Type Calculator)

### app-tv
- `minSdk 30`, `targetSdk 36`, `compileSdk 36`
- Manifest must declare:
  ```xml
  <uses-feature android:name="android.hardware.type.television" android:required="false" />
  <uses-feature android:name="android.software.leanback" android:required="false" />
  ```
- Launcher activity intent filter must include `android.intent.category.LEANBACK_LAUNCHER`
- Navigation: D-pad focus traversal only — no bottom nav, no gesture nav assumptions

---

## Key Dependencies

Always use these versions unless a newer stable version is explicitly confirmed:

```kotlin
// Compose
implementation("androidx.compose.bom:2025.05.00")
implementation("androidx.activity:activity-compose:1.10.1")

// TV Compose
implementation("androidx.tv:tv-foundation:1.0.0")
implementation("androidx.tv:tv-material:1.0.0")

// Navigation
implementation("androidx.navigation:navigation-compose:2.9.0")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")

// Hilt (annotation processing via KSP, not kapt)
implementation("com.google.dagger:hilt-android:2.56.1")
ksp("com.google.dagger:hilt-compiler:2.56.1")

// Retrofit
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")

// Room
implementation("androidx.room:room-runtime:2.7.1")
implementation("androidx.room:room-ktx:2.7.1")
ksp("androidx.room:room-compiler:2.7.1")

// Coil
implementation("io.coil-kt:coil-compose:2.7.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
```

---

## Architecture Rules

### Data Flow
```
PokeApiService (Retrofit) → Repository → UseCase → ViewModel → Composable
                Room (cache) ↗
```

- Repository is the single source of truth. Always check Room first; fetch from API on cache miss.
- Expose data from ViewModels as `StateFlow<UiState<T>>` where `UiState` is a sealed class with
  `Loading`, `Success`, and `Error` states.
- Never access the repository directly from a Composable.

### UiState Pattern
Every screen ViewModel must use this pattern:

```kotlin
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

All screens must handle all three states. Loading state should render skeleton placeholders, not
a spinner, wherever possible.

---

## core/data Module

### PokéAPI
- Base URL: `https://pokeapi.co/api/v2/`
- No authentication required
- All list endpoints are paginated — always implement `limit` and `offset` parameters
- Fetch full Pokémon detail lazily (on demand), not eagerly at list time

### Room Database (`PokedexDatabase`)
Entities to implement:
- `PokemonEntity` — id, name, spriteUrl, types (serialized), base stats (serialized), height,
  weight, cached timestamp
- `TypeEntity` — id, name
- `TeamEntity` — teamSlot (0–5), pokemonId (nullable)

Use `@TypeConverter` for lists and maps. Do not use `@Embedded` for nested data structures; keep
entities flat.

### Repository Interface Pattern
Define interfaces in `core/domain`, implement in `core/data`:

```kotlin
// core/domain
interface PokemonRepository {
    fun getPokemonList(limit: Int, offset: Int): Flow<List<Pokemon>>
    suspend fun getPokemonDetail(id: Int): Pokemon
    fun getTeam(): Flow<List<Pokemon?>>
    suspend fun setTeamSlot(slot: Int, pokemonId: Int?)
}
```

---

## core/domain Module

### Domain Models
Keep domain models clean — no Android or Room imports:

```kotlin
data class Pokemon(
    val id: Int,
    val name: String,
    val spriteUrl: String,
    val types: List<Type>,
    val stats: Stats,
    val height: Int,
    val weight: Int
)

data class Stats(
    val hp: Int, val attack: Int, val defense: Int,
    val specialAttack: Int, val specialDefense: Int, val speed: Int
)

enum class Type { NORMAL, FIRE, WATER, GRASS, ELECTRIC, ICE, FIGHTING, POISON,
    GROUND, FLYING, PSYCHIC, BUG, ROCK, GHOST, DRAGON, DARK, STEEL, FAIRY }
```

### TypeEffectivenessMatrix
Implement as a static object in `core/domain` — do not fetch this from the API at runtime:

```kotlin
object TypeEffectivenessMatrix {
    // Returns multiplier: 0f, 0.5f, 1f, or 2f
    fun effectiveness(attacking: Type, defending: Type): Float
    fun attackingEffectiveness(attacking: Type, defendingTypes: List<Type>): Float
    fun defendingWeaknesses(defendingTypes: List<Type>): Map<Type, Float>
}
```

---

## core/ui-common Module

### Theme
- Dark theme only — do not implement a light theme
- Type accent colors must be defined as named tokens, one per `Type` enum value
- Derive all colors from the wireframes — do not invent colors

Define type colors as:
```kotlin
object TypeColors {
    val Fire = Color(0xFFFF6B35)
    // ... one per type, derived from wireframes
}

fun Type.color(): Color = when(this) { ... }
```

### Shared Components
Implement these components before any screen work:

1. **`TypeBadge(type: Type, modifier: Modifier)`** — Pill shape, type color fill, white label,
   small caps. Derived from wireframes/components spec.

2. **`StatBar(label: String, value: Int, maxValue: Int = 255, color: Color, modifier: Modifier)`**
   — Horizontal bar, animated fill on composition. Label left, value right.

3. **`PokemonSprite(spriteUrl: String, contentDescription: String, modifier: Modifier)`** — Coil
   `AsyncImage` wrapper. Show a shimmer placeholder while loading.

4. **`SkeletonBox(modifier: Modifier)`** — Animated shimmer rectangle for loading states.

Do not add components to `ui-common` that are only used in one target. Keep it to genuinely shared
components only.

---

## Phone Screens (`app-phone`)

Implement in order:
1. Pokédex List
2. Pokémon Detail
3. Team Builder
4. Type Matchup Calculator

Each screen lives in its own package: `ui/list/`, `ui/detail/`, `ui/team/`, `ui/typecalc/`.

Each screen package contains:
- `[Screen]Screen.kt` — top-level Composable, consumes ViewModel state
- `[Screen]ViewModel.kt` — ViewModel, exposes StateFlow
- `[Screen]UiState.kt` — sealed UiState class if screen-specific state is complex

Bottom navigation destinations are defined in a `NavDestination` sealed class in `app-phone`.

---

## TV Screens (`app-tv`)

Implement in order:
1. Browse Grid
2. Pokémon Detail
3. Team Builder

### D-pad Focus Rules
- Every focusable element must have an explicit `Modifier.focusable()` or be a
  `tv-material` component that handles focus natively
- Use `FocusRequester` to restore focus position when returning from a detail screen
- Test all focus traversal paths: up/down/left/right/back
- Never assume touch input on the TV target

### TV-specific Layout
- Minimum touch target size does not apply — size for D-pad selection visibility instead
- Focus ring must be clearly visible against dark backgrounds — derive style from wireframes
- Sidebar filters should be collapsible to maximize grid space; default to collapsed

---

## Naming Conventions

| Thing | Convention | Example |
|---|---|---|
| Composable | PascalCase | `PokemonDetailScreen` |
| ViewModel | PascalCase + ViewModel | `PokemonDetailViewModel` |
| UiState | PascalCase + UiState | `PokemonDetailUiState` |
| Room entity | PascalCase + Entity | `PokemonEntity` |
| Repository interface | PascalCase + Repository | `PokemonRepository` |
| Repository impl | PascalCase + RepositoryImpl | `PokemonRepositoryImpl` |
| Use case | Verb + Noun + UseCase | `GetPokemonDetailUseCase` |
| Hilt module | PascalCase + Module | `DataModule` |

---

## What Not To Do

- Do not use `LiveData` — use `StateFlow` and `Flow` exclusively
- Do not use XML layouts — Compose only
- Do not use implicit comma joins in any SQL (`@Query` annotations) — explicit JOINs only
- Do not hardcode strings visible to the user — use `strings.xml`
- Do not access `Context` from a ViewModel — use Hilt to inject application-scoped dependencies
- Do not implement a light theme
- Do not add `www.` to the PokéAPI base URL
- Do not fetch `TypeEffectivenessMatrix` data from the network — it is static and hardcoded
- Do not create Room entities with nested objects — keep them flat with TypeConverters

---

## Definition of Done for Each Screen

A screen is considered complete when:
- [ ] All three UiState cases (Loading, Success, Error) are handled and visible
- [ ] Layout matches the wireframe for that screen
- [ ] Named design tokens from the wireframe map to named Kotlin constants
- [ ] ViewModel unit test exists covering state transitions
- [ ] No hardcoded strings (use `strings.xml`)
- [ ] Phone screens: tested at 360dp and 411dp width
- [ ] TV screens: D-pad traversal tested for all paths, focus never gets lost
