# Phase 2 development report

Branch: `development/native-android-phase2`, created directly from approved Phase 1 commit `fb3f65dc6c786cfe6721e880bc03dd5af49af793`. Work is committed locally only; no push or merge is part of this assignment. Phase 1 remains on its preserved branch. All changed files are under `native-android/`.

## Implemented behavior

- Playable Novice, Pro, Expert and Master setup with mode-specific hit/round limits, scratch placement, and adding/removing/renaming roster entries. Validation uses the engine configuration.
- Live scoring with current player/round, physical hits, scratches, Master pocketed balls and remaining-ball penalties, compact standings, hit undo, explicit standard scratch undo, Back, Done, Master saved-turn corrections, renaming and withdrawals.
- Final-round Master review and explicit finish confirmation showing all final scores and winners/ties. Finish before any complete round produces no history. Abandon discards without history. Dirty-turn corrections and destructive game actions require confirmation.
- Room-backed active restoration and immutable completed records. Every accepted game action is persisted before UI publication. DataStore-backed keep-screen-awake preference.
- Minimal History with winners/ties, totals and round details; minimal Settings. Compact scoring at normal heights; scroll fallback for short windows/large fonts.
- Centralized local-scoring/history feature flags; accounts, cloud synchronization and regional/global leaderboards remain disabled with no nonfunctional controls. No Firebase or Internet permission.

## Architecture and cloud-ready records

`:engine` remains Kotlin-only and independent of UI/storage. `:records` owns stable UUIDs, explicit ruleset and document versions, full configuration/roster/action history, event validation/replay and immutable completion snapshots. `:app` owns Compose, lifecycle ViewModel/StateFlow, repository implementations, Room and DataStore. Room uses KSP and exports its version 2 schema. Migration 1→2 preserves payloads; no destructive fallback is enabled.

Records contain local player IDs, display names, nullable account/region metadata, stable game/event IDs, start/completion timestamps, status, disabled sync eligibility/status and casual competitive metadata. Local UUIDs are not authenticated identities. The starting roster is immutable; rename/withdraw/correction actions are logged, and final names/scores/winner IDs are frozen in the completed snapshot. New games create new local roster identities rather than matching people by display name. All results remain casual and ineligible for competitive submission.

See `ARCHITECTURE.md` and `IOS_DATA_CONTRACT.md`. The independent JSON Schema and three golden examples support Swift/iOS consumers without Android dependencies. JSON schema version 1, ruleset `three-ball-2026.1`, and Room database version 2 are separate concepts.

## Persistence integrity and recovery

Active writes use expected-document/revision checks, and only an exact one-event continuation can replace a saved game. Finish/archive and active deletion run in one Room transaction. Completed rows use insert-with-abort semantics; an identical completion retry does not create a duplicate. Fault injection after archive insertion verifies that deletion failure rolls back both tables.

Loading replays accepted actions and validates all immutable result fields against the engine. Malformed/unsupported active payloads are retained in the recovery table and removed from the active slot; invalid completed rows remain untouched and are skipped with a notice. Database open, migration and save failures show retry/reload feedback without destructive reset. Preference corruption recovers to defaults. Explicit legacy and Android 12+ backup/transfer exclusions keep records local.

## Verification results

| Suite/check | Result |
| --- | --- |
| Pure Kotlin engine | 28 passed (all 26 Phase 1 tests plus 2 native correction tests) |
| Record replay/serialization/feature flags | 12 passed |
| ViewModel transitions, restoration, queued actions, failure handling | 8 passed |
| Room persistence, migration, immutable completion, rollback and invalid data | 9 passed |
| DataStore recreation/corruption | 2 passed |
| Robolectric Compose gameplay (Novice and Master through history) | 2 passed |
| Total automated JVM tests | 61 passed; 0 failures/errors/skips |
| Original JavaScript comparison | 16 scenarios passed; HTML unchanged |
| Independent JSON Schema/golden fixtures | 3 valid examples, future version and sync-eligibility rejection checks passed |
| Android debug APK | Built successfully, application ID `com.nodetoursstudio.threeball` |
| Android lint | 0 errors, 0 warnings |
| SDK-independent engine/records build | Verified with `-PengineOnly` |
| Diff/integrity checks | Only native changes; original HTML and Phase 1 branch unchanged |

Commands:

```sh
./gradlew -PengineOnly :engine:test :records:test
./gradlew :engine:test :records:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
node tools/verify-web-reference.mjs
python3 tools/verify-contract.py
```

Robolectric exercises real Room/SQLite and DataStore files on the host; gameplay smoke tests interact with Compose controls at 360×800 dp on an API 28 runtime. Build uses JDK 17, SDK 35 and build-tools 35.0.0. JSON Schema verification uses Python `jsonschema`. No generated build outputs, local SDK paths, debug keystores or credentials are committed. The exported Room schema is intentionally versioned source for migration review.

## Limitations and uncompleted verification

- No physical-device/emulator instrumentation or actual Android OS process-kill test was run. Persistence tests close/reopen the database, recreate stores/ViewModels, and replay interrupted/final-review states; physical-device lifecycle, orientation, keyboard, accessibility and font-scale acceptance remain release checks.
- No legacy web/Capacitor history import. The old Master migration discrepancy remains documented; native uses verified current gameplay rules.
- No end-user export/repair UI for quarantined records. Raw payloads remain in the local database for future support/recovery. Unsupported database versions fail safely rather than being erased.
- Event logs are JSON documents and are replayed during validation; exceptionally long games/history collections are not performance-benchmarked. History intentionally shows compact round details rather than a full event audit browser.
- Display names and scores are local observations, not identity/anti-cheat proof. Player accounts, synchronization, regional/global standings and competitive submission services are intentionally absent.
- The UI is English-only. Production signing/distribution and API-version/device-matrix testing are not included. Debug assembly is not a signed production release.

## Changed and added files

- `native-android/README.md`
- `native-android/app/build.gradle.kts`
- `native-android/app/schemas/com.nodetoursstudio.threeball.data.GameDatabase/2.json`
- `native-android/app/src/main/AndroidManifest.xml`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/GameViewModel.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/MainActivity.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/ThreeBallApplication.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/data/GameDatabase.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/data/LocalGameRepository.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/data/PreferencesRepository.kt`
- `native-android/app/src/main/res/values/strings.xml`
- `native-android/app/src/main/res/xml/backup_rules.xml`
- `native-android/app/src/main/res/xml/data_extraction_rules.xml`
- `native-android/app/src/test/java/com/nodetoursstudio/threeball/GameViewModelTest.kt`
- `native-android/app/src/test/java/com/nodetoursstudio/threeball/GameplayUiTest.kt`
- `native-android/app/src/test/java/com/nodetoursstudio/threeball/PersistenceTest.kt`
- `native-android/app/src/test/java/com/nodetoursstudio/threeball/PreferencesTest.kt`
- `native-android/app/src/test/resources/database-v1.sql`
- `native-android/build-engine.gradle.kts`
- `native-android/build.gradle.kts`
- `native-android/docs/ARCHITECTURE.md`
- `native-android/docs/IOS_DATA_CONTRACT.md`
- `native-android/docs/PHASE2_REPORT.md`
- `native-android/docs/SCORING_RULES.md`
- `native-android/docs/contract/game-record-v1.schema.json`
- `native-android/engine/src/main/kotlin/com/nodetoursstudio/threeball/engine/GameEngine.kt`
- `native-android/engine/src/test/kotlin/com/nodetoursstudio/threeball/engine/GameEngineTest.kt`
- `native-android/records/build.gradle.kts`
- `native-android/records/src/main/kotlin/com/nodetoursstudio/threeball/records/GameRecord.kt`
- `native-android/records/src/test/kotlin/com/nodetoursstudio/threeball/records/RecordTest.kt`
- `native-android/records/src/test/resources/contract/active-v1.json`
- `native-android/records/src/test/resources/contract/completed-v1.json`
- `native-android/records/src/test/resources/contract/master-completed-v1.json`
- `native-android/settings.gradle.kts`
- `native-android/tools/verify-contract.py`
