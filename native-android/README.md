# 3 Ball Scorekeeper — native Android foundation

No Detours Studio LLC · application ID `com.nodetoursstudio.threeball`

This independent Kotlin/Jetpack Compose project contains the first native increment: an Android shell, a pure Kotlin four-mode game engine, rule documentation, and tests. Existing web/Capacitor code is untouched. The UI is not yet a playable app.

## Build and test

Use JDK 17 or 21. Android builds require Android SDK platform 35 and build-tools 35.0.0. Set `ANDROID_HOME` or create an untracked `local.properties` with `sdk.dir=/your/sdk/path`.

```sh
cd native-android
./gradlew -PengineOnly :engine:test
./gradlew :app:assembleDebug :app:lintDebug
node tools/verify-web-reference.mjs
```

The Gradle 8.11.1 wrapper includes its distribution checksum. `-PengineOnly` excludes the Android module so rule tests can run without an SDK. The reference verifier also needs Node.js and the parent repository's `index.html`.

## Files

- `settings.gradle.kts`, `build.gradle.kts`, `build-engine.gradle.kts`, `gradle.properties`, `gradle/`, `gradlew*`: isolated build and wrapper.
- `app/build.gradle.kts`, `app/src/main/`: application ID, manifest, activity, four screen placeholders, resources, theme, and launcher icon.
- `engine/src/main/kotlin/.../GameModels.kt`: mode configuration, per-hit and turn records, game snapshots, history model, storage contract.
- `engine/src/main/kotlin/.../GameEngine.kt`: scoring, scratches, undo, turn advancement, back/corrections, withdrawals, and finish result.
- `engine/src/test/`: 26 JUnit tests and 16 shared reference scenarios.
- `tools/verify-web-reference.mjs`: read-only execution of reference scoring handlers.
- `docs/SCORING_RULES.md`: extracted rules, source anchors, migration discrepancy, and scope decisions.
- `docs/ARCHITECTURE.md`: module boundaries, persistence/ViewModel plan, and limitations.
- `docs/DEVELOPMENT_REPORT.md`: validation and recommended next steps.

See the rules document before changing the engine; Master history migrations do not use the same formula as current gameplay.
