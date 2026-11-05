# Phase 2.3 — Native Android Visual Richness

Status: implemented and verified; visual owner approval remains pending.

Branch: `development/native-android-ui-richness`.

Base: `1e5e1d67c7cc5bdbc5a9bb852e299ff9406f17bc` on `development/native-android-ui-polish`, retained unchanged.

Commit: resolve the local commit introducing this report with `git log -1 --format=%H -- native-android/docs/UI_RICHNESS_REPORT.md`. The final handoff supplies its full SHA. No push or merge was performed.

## Design package and scope

All five supplied PNGs were opened and inspected before source implementation. The approved specification, library index, mockups and handoff document were extracted byte-for-byte. The specification and index are in this directory, and all five images retain their `mockups/` paths. The package's root `PHASE_2_3_CODEX_HANDOFF.md` is preserved here rather than at repository root to comply with the user's native-only scope. Supplied document status text is unchanged; implementation status is recorded separately here. All eight supplied files match the ZIP bytes. The specification SHA-256 is `490abdd6a8c9d720892ad41c602c2b88fd9da08abf766bc653cb9c10b69dc76f`.

The documents provide visual guidance within the user's authorized task. They do not override Kotlin gameplay rules. Mockup illustrative winner/score inconsistencies, extra navigation and sample settings were not translated into game behavior.

## Implemented files and screen changes

| File | Presentation changes |
| --- | --- |
| `app/src/main/java/com/nodetoursstudio/threeball/ui/DesignSystem.kt` | Central neutral charcoal/slate, emerald felt, copper/gold, ivory and semantic action tokens; neutral root canvas, local textured hero, selective framing, neutral raised cards, serif player name and large tabular score, restrained decorative cue/balls and stat glyphs, separate confirmation-green Done, gold/check selected rounds, neutral navigation with selected emerald pill. Existing press animation timing, reduced-motion and accepted-feedback paths are retained. |
| `ui/LiveScreen.kt` | Framed current-turn section, distinct Done, raised red Finish using the existing confirmation, neutral Master chooser and dialogs. Existing controls, ordering, score calculations and commands remain intact. |
| `ui/SetupScreen.kt` | Gold mode emblems, clear checked felt selection, readable selected descriptions and adaptive game-option columns, neutral rules dialog. Existing mode defaults, limits, validation, player operations and scratch choices remain intact. |
| `ui/Standings.kt` | Neutral ranking rows and selective gold winner backgrounds/borders; rank, tie, eligibility and SCR/AVG/BEST calculations unchanged. |
| `ui/HistoryScreen.kt` | Concise collapsed player-score summaries; existing expanded ranking, round selection and immutable stored results retained. |
| `ui/ScorekeeperApp.kt`, `ui/ReviewPreviews.kt` | Use neutral canvas in actual app and existing Compose preview fixtures. No command or feedback semantics changed. |
| `app/src/main/res/values/themes.xml` | Match status/navigation bars to neutral canvas; existing safe-area handling retained. |
| `app/src/test/.../UiVisualReviewTest.kt` | Added selected Master setup and later-round History snapshots to existing responsive review tests. |
| `app/src/test/.../RichnessContrastTest.kt` | Three focused checks for readable enabled action text, neutral-surface secondary text and distinction between neutral canvas and felt. These are token checks, not a full per-pixel accessibility audit. |
| `docs/ui-review/richness/`, `README.md`, `docs/ui-review/README.md` | New paired host/device gallery and links; earlier phase galleries preserved. |

Paths abbreviated as `ui/` above are under the same Kotlin UI package. Settings and Scorecard use the reusable visual system without new screens or navigation changes. No external artwork dependencies, cloud services or fonts were introduced.

## Verification

Final command: `:engine:test :records:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon` using the native Gradle wrapper, JDK 17 and local Android SDK.

| Check | Result |
| --- | --- |
| Engine JVM tests | 28 passed |
| Records/serialization tests | 12 passed |
| App JVM/Robolectric tests | 60 passed |
| Total | **100 passed; zero failures, errors or skipped tests** |
| JavaScript comparison | **16 scenarios passed** against original `index.html` handlers via `tools/verify-web-reference.mjs` |
| Portable JSON | **3 fixtures valid**; unsupported future versions and synchronization eligibility rejected via `tools/verify-contract.py` |
| Android debug assembly | **BUILD SUCCESSFUL**; debug APK produced locally, not committed |
| Android lint | **Passed, zero reported issues** |

All prior tests remain. The five added cases are two UI render states and three contrast/token checks. Existing gameplay, saved-data rejection, migration, ViewModel, feature flag, persistence and serialization tests are retained.

Source protection review confirms engine, records, ViewModel, Room/DataStore/repositories, schema, feature flags, contracts, accepted-action haptics implementation, manifest and dependencies are unchanged from the approved base. The root web application and Capacitor code are unchanged. No generated build directories, APKs, database files, SDKs, local configuration, credentials or secrets are committed. Requested PNG review artifacts are documentation only.

## Visual evidence and emulator preservation

See [the side-by-side gallery](ui-review/richness/README.md) for approved mockups, 20 before/20 after deterministic host renders, and 23 actual Android emulator screenshots (12 baseline, including the pre-update preservation state, and 11 updated). Host fixtures cover default/Master Setup, zero/negative Master, chooser, Novice, Scorecard, collapsed/expanded/later-round History, Settings, short/tall/landscape windows, 130%/200% fonts, large roster and round strips.

Actual captures use Android 9/API 28 x86_64 software emulation at 360 × 800 pixels and 160 dpi, before and after an in-place update of the same application ID. No uninstall or data reset was performed. The baseline walkthrough saved a Novice result and a two-round Master result, then left an active Master turn at score −2 with one hit and three pocketed balls. After installing the new APK and cold launching, the active payload and both completed payloads were **exactly identical** to the pre-update values. SHA-256 of their sorted payload snapshot was `683fe4aa6bce171bbb8205d051ff25bbc8c58626757c972c82982c3cceb4e881` on both sides. Database copies used for verification remain local and are not committed.

The updated walkthrough exercises Undo/re-entry, chooser, History disclosure/round selection, Settings, turn progression, Scorecard, completion and new-game setup through the existing UI. Added test records are expected outcomes, not changes to existing results. A final database read confirms both original completed payloads are still identical, with one additional Master result and a newly active Novice game. The review gallery explicitly distinguishes real device screencaps from Robolectric host renders. The screenshot script initially missed Return to scoring after scrolling; the existing Android Back handler closed Scorecard and the walkthrough resumed, without changing app code.

## Differences and remaining acceptance checks

- Narrow Setup uses stacked complete field labels; two columns appear when inner width is at least 340 dp at normal font size. This prioritizes readability over the mockup's abbreviated two-column fields on every phone.
- Primary green is slightly darker than the suggested starting hex to keep ivory text contrast at least 4.5:1. Copper is concentrated on the hero; neutral sections use subtle slate borders.
- Native screen organization, defaults, lowest-score winner rules, eligible results, and existing controls supersede illustrative mockup sample values, extra filters and navigation labels. Decorative cues/icons are small local Canvas drawings, not imported raster backgrounds.
- Actual emulator review covers normal-font 360 × 800 gameplay. Larger phones, 130%/200% fonts, landscape, long rosters and round strips have host coverage. Real IME occlusion, TalkBack reading order, modern gesture-navigation/edge-to-edge insets and physical-device haptic quality/performance remain owner/device checks.
- Emulator animation scales were zero for stable capture; a software emulator cannot establish tactile feel. Existing automated tactile/haptic acceptance tests pass, and the prior animation/system reduced-motion logic is unchanged. Host pressed frames do not prove dynamic native-layer depth.
- A transient SystemUI ANR occurred during initial software cold boot; Wait allowed startup to finish. Captures and gameplay walkthrough used the warmed emulator. No game-engine/storage workaround or data reset was introduced.

The implementation follows the approved contrast and hierarchy rather than promising pixel identity with illustrations. Owner visual approval is still required before merge or release.
