# Native Android architecture — Phase 2

## Modules and direction

`:app` (Compose, lifecycle, Room, DataStore) → `:records` (portable record contract/replay) → `:engine` (pure Kotlin rules). The engine has no Android, UI, serialization, database, network, or clock dependency. Phase 1's 26 scoring tests remain unchanged, with additional native correction coverage. Records uses Kotlin serialization but no platform storage. No Firebase dependency, account client, synchronization service, or leaderboard implementation exists.

Normal builds share Android/Kotlin plugin classloaders. `-PengineOnly` selects the SDK-independent `build-engine.gradle.kts` and includes both pure JVM modules.

## UI and lifecycle

`ThreeBallApplication` owns a single lazy Room database and concrete repositories. `GameViewModel` receives repository interfaces, an injectable clock, and a worker dispatcher. It exposes read-only StateFlow, collected with `collectAsStateWithLifecycle`. A mutex serializes actions. Commands are evaluated/replayed off the main thread, persisted first, then published. A failed save leaves the previous displayed/persisted state intact and offers retry/reload. On creation the ViewModel loads Room and restores live play, cap/scratch latches, final review, and Master correction destinations by replay.

`MainActivity` hosts setup, scoring, history, and settings composables. Setup state uses rememberSaveable; a started game always restores from Room. Standard modes offer HIT/Scratch, hit undo, explicit scratch correction, Back and Done. Master has per-hit ball outcomes, attached scratch, undo, scorecard corrections and final-round review. Finish/discard/withdrawal actions use confirmation dialogs. Normal-height scoring keeps controls above a scrollable scorecard; short windows and large font scales permit scrolling so controls remain reachable. History and Settings intentionally remain small.

## Durable records and identities

`GameRecord` has UUID game/player/event IDs, versioned configuration, immutable initial roster, timestamps, complete action history and an immutable completed result. Current names and game state are projections, never an unvalidated stored score cache. Players remain indexed for the verified engine, while records/events reference stable IDs. Local roster IDs are assigned when starting a game and remain stable for its lifetime; they do not identify authenticated people or merge users with the same name across games.

`Records.state` validates IDs, versions, event sequence/time/position, all commands, terminal status and result consistency by replay. `RecordJson` tolerates additive fields but refuses unknown versions/actions. Golden JSON fixtures and the schema/contract are shared references for iOS. Read `IOS_DATA_CONTRACT.md` for details.

## Room and migration policy

`GameDatabase` is version 2, with an exported Room schema checked into `app/schemas/`:

- `active_game`: singleton slot, game ID, JSON payload, event revision, update time.
- `completed_games`: game ID primary key, immutable JSON payload, completion time.
- `quarantined_records`: unreadable active payloads retained for recovery.

`RoomGameRepository` is the application persistence boundary. It checks expected document/revision inside transactions, preventing stale commands from overwriting newer ones. Only an exact one-event continuation can replace active state. Finish inserts an immutable result and clears active state in one transaction; duplicate insert failures roll back, and a retry of the identical already committed result is idempotent. Finishing without a complete round clears active state without history. No destructive migration fallback is configured.

Phase 1 shipped no database. A reserved pre-release v1 payload schema is documented in `app/src/test/resources/database-v1.sql`; migration 1→2 adds timestamp columns with zero defaults and the recovery table, preserving payloads. Tests build a real v1 SQLite file from that fixture, migrate/open it through Room, validate the target schema and restore data. Future migrations must retain old schema fixtures and add tests. JSON document versions are separate from Room versions.

Unreadable active documents (including unsupported future versions) are moved transactionally to recovery storage before a new game can start. Invalid history rows remain untouched and are omitted from standings with a visible notice. No raw payloads or player data are logged. Database open/migration/I/O failures surface an error and retry, without resetting the database. This version has no end-user recovery/export tool; preserve the database for support or a future reader.

The original Phase 1 `engine.GameRepository` interface is retained for source compatibility; the application uses `data.LocalGameRepository`, whose records include the stable identity and event metadata absent from the Phase 1 interface.

## Preferences, backup and feature configuration

DataStore preferences hold keep-screen-awake. Defaults survive preference corruption using a corruption handler; I/O failure uses defaults and failed edits report errors. Preferences are separate from scoring records. Explicit legacy and Android 12+ backup/transfer rules exclude local files/databases/preferences; there is no automatic cloud upload or device transfer of records.

Central `records.Features` enables local scoring/history and disables accounts, cloud sync, regional and global leaderboards. Disabled features have no controls in the interface. Manifest contains no Internet permission. All games are casual and competitively ineligible; adding nullable account/region metadata does not authenticate local player IDs.

## Validation and limits

JUnit covers engine/scoring reference cases, record replay and golden compatibility, feature configuration, ViewModel transitions/failures, Room restart/migrations/atomic finish/corruption, DataStore recreation/corruption, and Compose gameplay smoke flows using Robolectric. The read-only Node verifier executes the original JavaScript scoring handlers on 16 shared scenarios.

The event log is replayed on load/action, and stored as one JSON document; this is suitable for local games but not optimized for extremely long sessions. No legacy web-history import or automatic reconciliation of the inconsistent old Master migration formula is included. Production signing, physical-device lifecycle testing, user-managed recovery/export, and localization remain future work. Android API 26+ is retained from the foundation.
