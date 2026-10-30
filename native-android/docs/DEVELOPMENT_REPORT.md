# First increment development report

Branch: `development/native-android-foundation` (local development branch, not main). Base: `03cc3d5`. No remote changes were pushed.

## Completed

Created an independent Kotlin/Jetpack Compose project under `native-android/`, using application ID `com.nodetoursstudio.threeball`, min SDK 26, target/compile SDK 35, AGP 8.9.1, Kotlin 2.1.20, Compose BOM 2025.03.01, and checksum-pinned Gradle 8.11.1.

Implemented a pure Kotlin game engine for Novice, Pro, Expert, and Master. It includes mode validation, physical shot caps, scratch rules, Master multi-ball hit records and remaining-ball penalties, undo, Done/round advancement, Back, Master scorecard corrections, name management, withdrawals, totals/ties/winner eligibility, final-round review, and explicit finish/archive eligibility. Added a repository contract for subsequent local persistence work.

Created a green-and-gold Compose shell with four screen boundaries and red 3-ball branding. These are navigable placeholders, not a finished scoring UI. Documented the reference rules and architecture before completing the engine translation. The rules document records the inconsistent legacy Master migration formula and the native correction guards.

## Validation

| Check | Result |
| --- | --- |
| Kotlin engine JUnit suite | 26 tests, 0 failures, 0 errors, 0 skipped |
| Original JavaScript scoring handler comparison | All 16 shared scenarios passed |
| Android debug assembly and lint | Debug APK built; lint: 0 errors, 1 backup-configuration warning |
| Android XML parsing | Passed |
| Staged whitespace check | Passed |
| Existing application integrity | `index.html` SHA-256 matches base commit |

Commands: `./gradlew -PengineOnly :engine:test`; `node tools/verify-web-reference.mjs`; `./gradlew :engine:test :app:assembleDebug :app:lintDebug`. Testing used an isolated SDK and a checksum-verified JDK 17 because the initial runtime lacked a Java compiler. Downloads required enabling network access on tool commands; no production build settings were altered for the environment.

`index.html` SHA-256: `742dea426cebe54821cfb502cb534cc63baa6660b04fa0e4b406f3264063d64e`. Only `native-android/` files are part of the changes. `www/` and the Capacitor Android project were not present in this checkout and were not created or modified.

## Limitations

- Lint warns that Android 12+ backup/transfer policy should be defined with `dataExtractionRules`; resolve alongside the durable storage design.

- No durable storage implementation yet; `GameRepository` is a contract. Active games and history are not persisted by the shell.
- Setup, scoring, history, and settings are placeholders. No ViewModel or playable controls are connected to the engine.
- No emulator/device testing, production signing, release APK, web-history import, legacy score migration, or manual standard scorecard editing.
- Node reference comparison exercises current scoring handlers on shared scenarios, not every UI interaction or legacy migration. Kotlin tests cover additional game transitions independently.
- Native blocks Finish and withdrawal during an open Master correction to protect the temporarily cleared entry. These deliberate guards and the reference's unusual Back-from-review behavior are documented in `SCORING_RULES.md`.

## Recommended next steps

1. Implement versioned local game/history storage with atomic finish-and-clear behavior, plus persistence, corruption, and process-recreation tests. Add DataStore-backed preferences.
2. Add a lifecycle ViewModel and wire setup/player editing, scratch placement, and mode-specific configuration to engine validation.
3. Wire Live Scoring to engine commands, including discard confirmations, Master hit outcomes, undo, corrections, and final-round review.
4. Implement history details and Settings, then review the web migration discrepancy before designing any import feature.
5. Run emulator/device accessibility and lifecycle checks and confirm minimum Android version and production signing requirements.

## Completed files

- `native-android/build-engine.gradle.kts` (alternate root build for SDK-independent engine tests)

- `native-android/.gitattributes`
- `native-android/.gitignore`
- `native-android/README.md`
- `native-android/app/build.gradle.kts`
- `native-android/app/src/main/AndroidManifest.xml`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/MainActivity.kt`
- `native-android/app/src/main/res/drawable/ic_three_ball.xml`
- `native-android/app/src/main/res/values/strings.xml`
- `native-android/app/src/main/res/values/themes.xml`
- `native-android/build.gradle.kts`
- `native-android/docs/ARCHITECTURE.md`
- `native-android/docs/DEVELOPMENT_REPORT.md`
- `native-android/docs/SCORING_RULES.md`
- `native-android/engine/build.gradle.kts`
- `native-android/engine/src/main/kotlin/com/nodetoursstudio/threeball/engine/GameEngine.kt`
- `native-android/engine/src/main/kotlin/com/nodetoursstudio/threeball/engine/GameModels.kt`
- `native-android/engine/src/test/kotlin/com/nodetoursstudio/threeball/engine/GameEngineTest.kt`
- `native-android/engine/src/test/resources/scoring-vectors.tsv`
- `native-android/gradle.properties`
- `native-android/gradle/wrapper/gradle-wrapper.jar`
- `native-android/gradle/wrapper/gradle-wrapper.properties`
- `native-android/gradlew`
- `native-android/gradlew.bat`
- `native-android/settings.gradle.kts`
- `native-android/tools/verify-web-reference.mjs`
