# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

YuNao (渔脑, "Fish Brain") — a native Android fishing-companion app, `versionName = "1.0.0"`. Positioning: a personal fishing log that predicts your own hit rate at your own spots from your own history, instead of a generic community fishing-index app (e.g. 钓鱼佬). All user data (logs, spots, gear, trips) is stored on-device in Room — no login, no cloud backend. Weather (per-log snapshot) and nearby-spot discovery are the two features that do call the network; everything else works offline.

## Commands

```bash
./gradlew assembleDebug      # build debug APK -> app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease    # build release APK -> app/build/outputs/apk/release/
./gradlew installDebug       # build and install to a connected device/emulator
```

Windows: use `gradlew.bat` instead of `./gradlew`. There are no test source sets (`app/src/test`, `app/src/androidTest` don't exist) and no lint/ktlint config — don't invent test or lint commands.

CI (`.github/workflows/build-apk.yml`) builds the debug APK on every push to `main`. Release signing reads `YUNAO_KEYSTORE_PATH` / `YUNAO_KEYSTORE_PASSWORD` / `YUNAO_KEY_ALIAS` / `YUNAO_KEY_PASSWORD` from the environment (see `app/build.gradle.kts`); without those set, release builds are unsigned.

**This repo moves fast and mostly outside of any one Claude session.** Commits land directly on `main` from multiple tools/sessions the same day. Before starting nontrivial work, `git fetch && git log --oneline HEAD..origin/main` to check you're not about to duplicate or conflict with work that already landed upstream.

## Architecture

Composition root: `MainActivity` calls `LocalRepository.init(applicationContext)` before `setContent`, then `YuNaoRoot` (wraps `YuNaoTheme`) → `YuNaoApp` in [Navigation.kt](app/src/main/java/com/yunao/fishing/ui/Navigation.kt), which hosts a `Scaffold` with a bottom `NavigationBar` and a `NavHost` wiring 6 routes to 6 top-level screens under `ui/screens/`: `home`, `log`, `spots`, `community`, `gear`, `profile`.

**Persistence: Room, behind one repository facade.**
- [data/LocalModels.kt](app/src/main/java/com/yunao/fishing/data/LocalModels.kt) — `@Entity` classes (`CatchLogEntry`, `UserSpot`, `UserGearItem`, `Trip`) plus non-persisted result shapes (`NearbySpot`, `ForecastFactor`, `SpotForecast`).
- [data/AppDatabase.kt](app/src/main/java/com/yunao/fishing/data/AppDatabase.kt) — Room database (`yunao.db`, version 2, `fallbackToDestructiveMigration`) with one DAO per entity.
- [data/LocalRepository.kt](app/src/main/java/com/yunao/fishing/data/LocalRepository.kt) — the only thing screens talk to. Must call `LocalRepository.init(context)` once (done in `MainActivity`) before any other call. Also owns a device-anonymous `currentUser` (a UUID persisted in `SharedPreferences`, no auth) and the nickname setting. Method names intentionally mirror an earlier `FirebaseRepository` (see git history: the project briefly used Firebase, then moved to local-only storage) in case cloud sync ever comes back.
- Screens follow one consistent pattern: `remember { mutableStateOf(...) }` + `LaunchedEffect(reloadKey)` to load from `LocalRepository`, `rememberCoroutineScope().launch { ... ; reloadKey++ }` for writes (add/delete/join), and an `AlertDialog`-based `Add*Dialog` composable for creation forms. Follow this pattern for new CRUD features rather than introducing a ViewModel layer.

**Forecasting is 100% derived from the user's own logs, by design — no external weather/fishing-index API feeds it.** [data/ForecastEngine.kt](app/src/main/java/com/yunao/fishing/data/ForecastEngine.kt) takes `List<CatchLogEntry>` and computes per-spot hit rate plus the strongest contributing factor (wind direction / sky / pressure trend) once a spot has ≥3 logs. `HomeScreen` just loads logs and calls `ForecastEngine.buildForecasts(logs)` — there is no more mock forecast data. Keep this engine free of network calls; it's a deliberate product differentiator ("uses your own history, not a generic index"), not an oversight.

**Two features are live/networked, both key-less and both explicitly designed as "don't make the user type this in":**
- [data/LocationHelper.kt](app/src/main/java/com/yunao/fishing/data/LocationHelper.kt) — one-shot GPS/network location fix via plain `android.location.LocationManager` (no Play Services / Fused Location). 8s timeout, falls back to a cached fix. Callers must already hold `ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION` (all of `SpotsScreen`, `LogScreen`'s add-dialog, and `AddSpotDialog` request it themselves via `rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions())`, following the same inline pattern each time).
- [data/NearbySpotsRepository.kt](app/src/main/java/com/yunao/fishing/data/NearbySpotsRepository.kt) — `SpotsScreen`'s "搜索附近" auto-discovers fishing spots/tackle shops near the user via the OpenStreetMap Overpass API (`leisure=fishing`, `shop=fishing`, `sport=fishing`, `water`+`fishing` tags within 8km), trying 3 mirror endpoints in order. Returns `Result<List<NearbySpot>>` — **`Result.success(emptyList())` means "OSM genuinely has no data here" (common in China), `Result.failure(...)` means the request itself failed (network/timeout/blocked).** Always branch on both in the UI; collapsing them into one message hides a debuggable failure behind "no data found". OSM coverage for fishing POIs in China is sparse — if "no data" reports keep coming from real usage, the actual fix is likely swapping/adding a China-native POI provider (e.g. Amap/高德, which needs a registered API key) rather than tuning the Overpass query further.
- [data/WeatherRepository.kt](app/src/main/java/com/yunao/fishing/data/WeatherRepository.kt) — `LogScreen`'s add-log dialog auto-fetches current sky/wind/pressure-trend from Open-Meteo the moment the dialog opens (no button needed) and pre-selects the matching `ChipGroup` options; the user can still override any chip. This only automates *data entry at log time* — it does not feed `ForecastEngine`, so it doesn't conflict with that engine's "no external weather API" design principle. Bucketing (`skyFromCode`/`windDirBucket`/`windForceBucket`) is deliberately coarse to match the existing 4-6 option chip vocab.

Both networked repositories use OkHttp (`com.squareup.okhttp3:okhttp`) + `org.json`; no Retrofit/Moshi. Follow that convention for new API calls rather than introducing a new HTTP stack.

`GearScreen` still reads static recommendation content from `MockData.gearPlans` in [data/Models.kt](app/src/main/java/com/yunao/fishing/data/Models.kt) — that's the one legitimate remaining use of the old mock-data object; everything else in `MockData` is dead (superseded by the Room-backed screens) and slated for removal.

Theming lives in `ui/theme/` ([Theme.kt](app/src/main/java/com/yunao/fishing/ui/theme/Theme.kt), [Type.kt](app/src/main/java/com/yunao/fishing/ui/theme/Type.kt)): custom light/dark `ColorScheme` (DeepSea/ReedGreen/SunsetAmber/MistGray), Android 12+ dynamic color support gated behind a `dynamicColor` flag defaulted to `false`.

Manifest permissions: `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`. No storage/camera permissions yet — photo capture for catch logs is not implemented.

Stack: Kotlin 1.9.24, AGP 8.5.2, Compose BOM 2024.06.00, Material 3, Navigation-Compose 2.7.7, Room 2.6.1 (KSP, not kapt — the root `build.gradle.kts` applies `com.google.devtools.ksp`), OkHttp 4.12.0, `minSdk` 26 / `compileSdk`&`targetSdk` 34, Java/Kotlin target 17. Package/namespace: `com.yunao.fishing`.
