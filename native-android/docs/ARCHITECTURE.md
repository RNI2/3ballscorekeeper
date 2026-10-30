# Native foundation

`-PengineOnly` selects `build-engine.gradle.kts` as the root build and excludes `:app`; normal builds share Android and Kotlin plugin classloaders in `build.gradle.kts`.

## Boundaries

- `:engine`: Kotlin/JVM only. `GameConfig`, `Turn`, `Hit`, `Player`, `GameState`, and archive models; pure `GameEngine` transitions; JUnit tests. No Android, DOM, clock, database, or coroutine runtime dependency.
- `:app`: Android activity and Compose screen shell with separate setup, live scoring, history, and settings composable boundaries. Green table colors, gold accents, and a red 3-ball launcher drawable. It deliberately does not expose a playable game yet.
- `GameRepository`: suspend contract for loading and saving the active snapshot, history, finishing, and abandoning. Concrete durable storage is deferred to the next increment, not claimed as complete.

State is passed into each transition and a new snapshot is returned. Nullable saved turns distinguish an unplayed turn from a valid zero or negative Master score. Stable player positions retain withdrawn players in history. UI code must treat lists as immutable; persistence adapters must validate restored snapshots before passing them to the engine.

## Next increment architecture

Add a repository implementation with versioned serialization and atomic persistence (Room transactions for game records/history, DataStore for settings), then a lifecycle ViewModel that owns StateFlow UI state and serializes commands. Persist each accepted command before exposing success; finish must atomically archive eligible results and clear active state. Keep clock and IDs outside the engine. Add process-recreation, storage round-trip, corruption/version handling, and transactional finish tests with that implementation.

Wire Game Setup and Live Scoring first. UI derives enabled actions from mode/cap/draft, offers scratch placement, and requests discard confirmation before calling correction APIs with `discardDraft=true`. Build history/settings features in subsequent increments. Avoid duplicate scoring calculations in composables.

## Deliberate first-task limitations

No durable storage adapter, interactive setup/scoring, ViewModel, settings editor, web history import, standard manual score editing, or production signing. Four screen destinations are foundation placeholders. Minimum Android API 26 is an initial project choice; confirm supported devices before release. Android SDK/AGP/Kotlin/Compose versions are pinned, and updates can be reviewed independently of the production web project.
