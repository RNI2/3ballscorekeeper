# Phase 2.2 — Native Android Premium UI Polish

**Status:** implemented and tested; visual acceptance pending owner review in Android Studio.

**Branch:** `development/native-android-ui-polish`

**Base:** `f04873d7bded804f66e9ee610abb818583282265`

**Commit:** the single local commit introducing this report; resolve its full SHA with `git log -1 --format=%H -- native-android/docs/UI_POLISH_REPORT.md`. The handoff also reports the SHA. No push, merge or release is authorized by this work.

The approved `UI_POLISH_SPEC.md` was copied unchanged before implementation. Existing Play / History / Settings navigation, dedicated scorecard access, engine transitions, persistence, identities, feature flags and portable contracts are preserved.

## Completed polish and component inventory

| Component/file | Result |
| --- | --- |
| `ui/DesignSystem.kt` | Rich emerald/deep-felt palette, lightweight grain, copper/mahogany framing with fine edge highlights, serif heading strips, glossy local 3-ball, raised `FeltCard`, tabular score/stat typography, aligned spacing and a selected navigation pill with billiards/clock/gear icons. |
| `TactileActionButton` and action wrappers | Raised primary/secondary/support/destructive surfaces, shallow bevel/highlight, 100 ms elevation and label depression/release; no repeated pulses or animation-generated actions. Surface hit targets remain at least 48 dp (primary 60 dp). |
| `SelectionPanel` | Filled selected state, explicit check/selected semantics and a restrained 120 ms edge emphasis. No color-only selection or persistent glow. |
| `ui/LiveScreen.kt` | Compact Current Turn context, immediate signed score, aligned metrics, full-width primary Hit, paired Undo/Scratch, aligned Back/Done, expandable hit summary and compact standings. Scorecard/finish share a secondary footer. Short windows/large fonts scroll without shrinking controls. |
| `ui/SetupScreen.kt` | Four dimensional difficulty cards, separately grouped contextual game options, equal-weight scratch-placement cards (stacked for large fonts), roster validation, blue Add Player and clear Start Game. Defaults/limits still come from the existing engine. |
| `ui/Standings.kt` | Shared compact standings, earned local rank medals including ties, nullable unplayed totals, derived SCR/AVG/BEST, horizontally scrollable wide rankings, selected round strips and reusable round/hit summaries for scorecard/history. |
| `ui/HistoryScreen.kt` | Compact date/mode/winner snapshots; only one expanded game at a time; ranking columns, explicit swipe hint, round navigation and saved Master hit details. Immutable result documents are never edited by display interactions. |
| `ui/AcceptedFeedback.kt`, `ui/ScorekeeperApp.kt` | Success haptics are acknowledged after one accepted, published saved HIT/SCRATCH/DONE event; never on chooser opening, cancellation, disabled taps, failed saves, reload, tab changes or recomposition. Existing ViewModel/save-before-publish behavior is unchanged. |
| Settings and bottom navigation | Consistent framing/type/selection hierarchy, original keep-screen-awake preference and offline-only messaging. No fake account/sync/export/leaderboard controls. |

Stats are read-only projections from saved engine turns or immutable final results. Saved totals/winner eligibility remain authoritative. AVG uses only that player's saved turns, BEST includes zero/negative scores, SCR excludes discarded/undone draft penalties, and withdrawn players retain scores without earning a current rank. No data backfill, migration or new scoring feature was introduced.

## Feedback and accessibility behavior

- Pressed artwork changes immediately; subtle elevation/scale releases use 100 ms transitions. Only the artwork/label/elevation changes; clickable geometry is not scaled below its minimum target size.
- The system `ValueAnimator.areAnimatorsEnabled()` gate and a testable local motion policy disable nonessential scale motion when animations are disabled. Selection transitions become immediate in that state.
- Accepted-event haptics use `View.performHapticFeedback(KEYBOARD_TAP)` without ignore-settings flags or a new permission. Android decides whether device/view/global haptics allow it. A test injection verifies the acknowledgement path without claiming to emulate a motor.
- The displayed score is never interpolated through invented numeric values; it updates immediately from the published engine state. Animations never gate persistence or generate commands. Existing rapid-tap UI serialization is retained.
- 130%/200% font, short/tall phones, landscape, a long active name and six-player data have host-render evidence. Existing larger-font reachability tests remain. Paired action rows retain equal heights when text wraps.
- Rankings retain wide readable columns and a visible horizontal-swipe hint. Short round strips avoid unnecessary nested horizontal scrolling; longer/large-font strips remain horizontally scrollable.

## Test and build results

| Verification | Result |
| --- | --- |
| Engine | 28 passed, unchanged |
| Records/feature flags/JSON compatibility | 12 passed, unchanged |
| ViewModel | 8 passed, unchanged |
| Room/migration/finish integrity | 9 passed, unchanged |
| DataStore | 2 passed, unchanged |
| Gameplay Compose | 9 passed, including failed-save/no-haptic, successful Hit/Scratch/Done acknowledgements and no repeat on reload/navigation |
| Presentation projection/feedback policy | 4 passed: negative/zero/unplayed statistics, tied/withdrawn ranks, event acknowledgement guards |
| Tactile controls | 3 passed: pressed/released state, exactly one click, disabled behavior and reduced-motion scale |
| History presentation | 2 passed: one expanded game, ranking columns/selected rounds and byte-identical result after browsing |
| Native host rendering | 18 passed, including held Hit and responsive/long-name/round-focus evidence |
| Total automated tests | **95 passed; 0 failures/errors/skips** |
| JavaScript reference comparison | **16 scenarios passed** |
| Independent JSON Schema fixtures | **3 examples passed**, plus future-version/sync-eligibility rejection checks |
| Android debug assembly | **Passed**; `com.nodetoursstudio.threeball` unchanged |
| Android lint | **0 errors, 0 warnings** |

Commands:

```sh
./gradlew :engine:test :records:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
node tools/verify-web-reference.mjs
python3 tools/verify-contract.py
```

Testing used JDK 17, SDK 35/build-tools 35.0.0, and the API 28 Robolectric host runtime. No new runtime or test dependencies were required.

## Visual evidence

`ui-review/polish/README.md` is the comparison index. It contains **12 before + 18 after PNGs** covering Setup, Novice, Master idle/negative/chooser, Scorecard, History and Settings, plus responsiveness, pressed feedback, rankings and round detail. Before captures were frozen from the `f04873d7` presentation before token changes; added Novice/Settings fixtures reused that original UI. Existing Phase 2.1 evidence remains untouched.

`ui/ReviewPreviews.kt` contains **15 named Android Studio previews**. `UiVisualReviewTest` can regenerate the 18 after renders into the ignored build directory. The sample records are created locally through the unchanged engine/records layer; they are not real user history or competitive results.

## Deviations, limitations and required owner acceptance

1. **No actual emulator/device screenshots or manual device walkthrough:** this environment has no available hardware-accelerated emulator/KVM. Evidence is explicitly labeled as Robolectric native-graphics host rendering/Compose preview, with approximated modal-window dimming. It does not establish device visual approval.
2. **Physical haptics and system settings need a device check.** The held-Hit host PNG is pixel-identical to idle on this renderer, so it is not evidence of the dynamic pressed appearance; transient layers/elevation need device review. Component tests separately verify pressed/released state and reduced-motion scale. Host tests verify accepted-event timing, failure/reload suppression, disabled controls and the reduced-motion component override. Hardware feel, real system animation-scale changes, TalkBack, real IME/cutouts, device GPU/font differences and performance remain owner acceptance checks.
3. **Scrolling remains intentional for constrained layouts.** At default 360×800, the reviewed Master negative-score state shows the player, metrics and all frequent controls together, with standings and scorecard/finish access. Short screens, long names, expanded logs and large fonts scroll naturally instead of clipping or shrinking targets. Wide ranking columns scroll horizontally.
4. The proposed color values were adjusted conservatively for ivory-text contrast, particularly action green. The score does not animate through intermediate numeric values; tactile surface/selection animation supplies motion without compromising accuracy.
5. The task supplied the approved polish spec but no separate iPhone screenshot files; the repository web CSS and preserved review evidence informed the visual adaptation. Owner comparison against the original captures is still required.

No functional-core defect was changed under this assignment. Protected engine/records/ViewModel/repository/Room/DataStore/schema/manifest files were checked against the baseline, and remain byte-identical. The uploaded specification also matches its original bytes, including intentional Markdown hard breaks. Production web/Capacitor code, the approved baseline branch and existing data contracts are untouched. No build directories, APKs, SDK paths, debug keys, credentials or backend services are committed; requested review PNGs are documentation-only artifacts.

## Files changed/added

- `native-android/README.md`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/ui/AcceptedFeedback.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/ui/DesignSystem.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/ui/HistoryScreen.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/ui/LiveScreen.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/ui/ReviewPreviews.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/ui/ScorekeeperApp.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/ui/SetupScreen.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/ui/Standings.kt`
- `native-android/app/src/test/java/com/nodetoursstudio/threeball/GameplayUiTest.kt`
- `native-android/app/src/test/java/com/nodetoursstudio/threeball/HistoryPolishTest.kt`
- `native-android/app/src/test/java/com/nodetoursstudio/threeball/PolishPresentationTest.kt`
- `native-android/app/src/test/java/com/nodetoursstudio/threeball/TactileButtonTest.kt`
- `native-android/app/src/test/java/com/nodetoursstudio/threeball/UiVisualReviewTest.kt`
- `native-android/docs/UI_POLISH_REPORT.md`
- `native-android/docs/UI_POLISH_SPEC.md`
- `native-android/docs/ui-review/README.md`
- `native-android/docs/ui-review/polish/README.md`
- `native-android/docs/ui-review/polish/after/history-expanded.png`
- `native-android/docs/ui-review/polish/after/history-round.png`
- `native-android/docs/ui-review/polish/after/history.png`
- `native-android/docs/ui-review/polish/after/hit-pressed.png`
- `native-android/docs/ui-review/polish/after/master-after-hit.png`
- `native-android/docs/ui-review/polish/after/master-chooser.png`
- `native-android/docs/ui-review/polish/after/master-font-130.png`
- `native-android/docs/ui-review/polish/after/master-font-200.png`
- `native-android/docs/ui-review/polish/after/master-idle.png`
- `native-android/docs/ui-review/polish/after/master-landscape.png`
- `native-android/docs/ui-review/polish/after/master-short.png`
- `native-android/docs/ui-review/polish/after/master-tall.png`
- `native-android/docs/ui-review/polish/after/novice.png`
- `native-android/docs/ui-review/polish/after/scorecard-round.png`
- `native-android/docs/ui-review/polish/after/scorecard.png`
- `native-android/docs/ui-review/polish/after/settings.png`
- `native-android/docs/ui-review/polish/after/setup.png`
- `native-android/docs/ui-review/polish/after/six-players.png`
- `native-android/docs/ui-review/polish/before/history.png`
- `native-android/docs/ui-review/polish/before/master-after-hit.png`
- `native-android/docs/ui-review/polish/before/master-chooser.png`
- `native-android/docs/ui-review/polish/before/master-font-200.png`
- `native-android/docs/ui-review/polish/before/master-idle.png`
- `native-android/docs/ui-review/polish/before/master-landscape.png`
- `native-android/docs/ui-review/polish/before/master-short.png`
- `native-android/docs/ui-review/polish/before/master-tall.png`
- `native-android/docs/ui-review/polish/before/novice.png`
- `native-android/docs/ui-review/polish/before/scorecard.png`
- `native-android/docs/ui-review/polish/before/settings.png`
- `native-android/docs/ui-review/polish/before/setup.png`
