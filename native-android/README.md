# 3 Ball Scorekeeper — native Android

No Detours Studio LLC · `com.nodetoursstudio.threeball` · Android API 26+

Phase 2 adds a playable offline app on top of foundation `fb3f65d`. All development is isolated in `native-android/`. The web application and Capacitor implementation are untouched.

## Play

Choose Novice, Pro, Expert or Master, scratch placement and 2–6 players (Master up to 20). Configure Pro/Master hit limits and optional Master rounds. Start, record hits/scratches, correct mistakes, and tap Done to advance. Master records balls pocketed per hit and supports saved-turn correction. The scorecard supports renaming and withdrawing players. Finish explicitly to archive; at least one complete round is required. Interrupted games resume from local storage. History includes ties and round details. Settings controls screen-awake behavior.

Local games use Room, preferences use DataStore, and scoring is a pure Kotlin engine. No account, Firebase, network service, credentials, automatic upload or competitive submission is included. All results are casual and local.

## Build and verify

Use a full JDK 17 or 21 (including javac), Android SDK platform 35/build-tools 35.0.0, and `ANDROID_HOME` or an untracked `local.properties`. Gradle 8.11.1 is checksum-pinned.

```sh
cd native-android
./gradlew -PengineOnly :engine:test :records:test
./gradlew :engine:test :records:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
node tools/verify-web-reference.mjs
python3 tools/verify-contract.py  # requires Python jsonschema
```

Robolectric tests run Room/SQLite migration/restart, preferences, and Compose gameplay on the JVM; their Android runtime artifacts download on first use. Device/emulator testing is a separate release check. The Node verifier reads the parent repository's `index.html` without changing it.

## Code and documents

- `engine/`: game rules and original scoring tests, plus explicit native scratch correction.
- `records/`: stable IDs, versioned JSON, event replay, immutable results, feature flags, contract tests/fixtures.
- `app/src/main/`: Compose screens, ViewModel, Room repositories/schema migration, preferences and backup policy.
- `app/src/test/`: persistence/migration, ViewModel, preferences and Compose smoke tests.
- `app/schemas/`: exported Room schema; never commit generated build directories.
- `docs/SCORING_RULES.md`: verified rules and reference discrepancies.
- `docs/ARCHITECTURE.md`: lifecycle, storage, recovery, migrations and module boundaries.
- `docs/IOS_DATA_CONTRACT.md`, `docs/contract/game-record-v1.schema.json`: independent iOS serialization/replay contract.
- `docs/PHASE2_REPORT.md`: implemented files, results and limitations.
- `docs/DEVELOPMENT_REPORT.md`: historical Phase 1 report, retained unchanged.

## Phase 2.1 presentation redesign

The compact pool-table UI is isolated under `app/src/main/java/com/nodetoursstudio/threeball/ui/`, while the Activity delegates to the existing ViewModel. Scoring, Room/DataStore and portable records are unchanged. The approved specification is preserved verbatim in `docs/UI_DESIGN_SPEC.md`. See `docs/UI_REDESIGN_REPORT.md` and `docs/ui-review/README.md` for scope, verification, before/after renders and outstanding emulator/device acceptance.

## Phase 2.2 premium polish

Presentation tokens, tactile controls and accepted-action haptics are implemented without changing the engine, persistence, feature flags or contracts. Compact local standings and selectable round summaries are reused by scoring/history; derived SCR/AVG/BEST values never modify results. See `docs/UI_POLISH_SPEC.md`, `docs/UI_POLISH_REPORT.md` and `docs/ui-review/polish/README.md` for the approved direction, validation and before/after evidence. Owner/device visual acceptance is still required.

## Phase 2.3 visual richness

The neutral charcoal and local emerald design system preserves scoring and storage behavior. See `docs/UI_RICHNESS_SPEC.md`, `docs/UI_RICHNESS_REPORT.md` and `docs/ui-review/richness/README.md` for approved references, automated verification and paired host/emulator evidence. Owner visual approval remains pending.

## Phase 2.4 official branding

Shared startup/header identity and the supplied official No Detours Studio artwork are documented in `docs/BRAND_SPLASH_REPORT.md`, `docs/brand/README.md` and `docs/brand/review/README.md`. Owner visual approval remains pending.
