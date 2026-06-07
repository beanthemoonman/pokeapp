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
