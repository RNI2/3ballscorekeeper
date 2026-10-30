# Phase 2.1 native UI redesign report

Branch: `development/native-android-ui-redesign`. Base: approved Phase 2 commit `ce7917ac6605fb8f64b447d659eaa8ab1156584d`. The uploaded `UI_DESIGN_SPEC.md` was saved verbatim to its requested path before implementation. No push or merge is included.

## Completed presentation changes

- Extracted the UI from the Activity into `ui/`. Added reusable felt/cream/gold/copper design tokens, system serif heading typography, lightweight felt gradients/stripes, red three-ball branding, tactile button variants, framed rail sections, teal player cards with blue/earned-green accent rails, stat tiles, leader preview, round progress, and labeled icon navigation.
- Setup now presents four full-width difficulty cards with contextual Rules dialogs, engine-validated mode fields, two explicit scratch-placement cards, editable roster, add/remove actions and a branded Start Game control. It scrolls normally and respects IME insets.
- Live Scoring prioritizes current player, prominent signed score, round/player progress and metrics. Hit, Undo, scratches and Done remain large; Back is still an explicit correction action. The full scorecard is now a separate sub-screen instead of a permanently expanded table.
- Master now opens a real modal ball chooser after Hit. Opening/canceling records nothing. Valid outcomes use 2×2 large controls; selection goes through the original ViewModel. Last-hit scratch, attached-scratch undo, remaining-ball penalty, hit history, saved-turn reopening, final review and negative scores preserve existing semantics.
- Scorecard/Players retains names, totals, leader/current status, withdrawal/rename confirmation, round values, per-hit details and guarded Master edits. Android system Back returns to scoring; it never issues the game's correction Back command.
- History and Settings use the same rail/teal identity, with an intentional empty-history state, local casual result details, recovery notices, and the existing screen-awake option. No disabled online features appear.
- Added a synchronous UI click gate around the unchanged command flow so rapid taps cannot enqueue another mutation while an accepted UI action is pending. It releases after the existing ViewModel flow settles, including failures. No engine debounce or rule change was introduced.
- Large fonts stack stat tiles; short/landscape screens scroll instead of shrinking touch targets. Scores announce player/value through polite accessibility semantics, decorative logos are excluded from TalkBack, chooser buttons identify balls pocketed, and selection/leader/current states include text rather than only color.

## Preserved invariants

Engine sources/tests, records/feature flags/fixtures, ViewModel, repositories, Room schema/migrations, DataStore, app ID, manifest permissions and portable JSON contract are byte-for-byte unchanged from the approved baseline. The two original gameplay UI tests were adapted to scrolling and Master’s newly specified two-step presentation, while retaining their scoring/completion assertions. All other existing tests are unchanged.

`index.html`, web/Capacitor paths and the Phase 2 branch remain unchanged. All committed changes are inside `native-android/`. No new runtime dependencies, network artwork, fonts, backend features, credentials or build outputs are added. Source visual tokens were inspected in the root web CSS; the screenshot references described by the spec were not separately attached to this task.

## Verification

| Check | Result |
| --- | --- |
| Engine tests | 28 passed, unchanged |
| Records/serialization/feature flags | 12 passed, unchanged |
| ViewModel | 8 passed, unchanged |
| Room/migrations/completion integrity | 9 passed, unchanged |
| DataStore | 2 passed, unchanged |
| Gameplay Compose tests | 8 passed: original flows plus chooser cancellation, system Back, Pro/Expert caps, short height, Master scratch/undo/correction and 200% font reachability |
| Host visual rendering | 10 tests passed: production Compose previews at normal/short/tall/landscape sizes, modal chooser and large fonts |
| Total | 77 passed; zero failures, errors or skipped tests |
| Original web comparison | 16 scenarios passed |
| Portable JSON schema examples | 3 examples passed, including future-version/sync-eligibility rejection checks |
| Debug APK | `:app:assembleDebug` passed; app ID unchanged |
| Lint | `:app:lintDebug` passed, 0 issues |
| Scope/integrity | Protected business/data files and Phase 2 branch unchanged; specification copy matches uploaded bytes |

Commands: `./gradlew :engine:test :records:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`; `node tools/verify-web-reference.mjs`; `python3 tools/verify-contract.py`.

## Visual review material

See `ui-review/README.md` for the gallery and capture provenance. Thirteen PNGs include three baseline before renders and ten after renders. Ten Android Studio Compose previews live in `ui/ReviewPreviews.kt`. These render the actual production components with local sample records. PNGs are documentation, not runtime assets or test build outputs.

Before images were rendered from the exact Phase 2 presentation read from Git in a temporary test harness, which was removed before commit. The after visual test harness remains available to regenerate previews under the ignored build directory. Screenshots are host captures, not pixel-golden assertions.

## Specification deviations and unverified acceptance

1. **No Android emulator/device walkthrough or emulator screenshots were produced.** This environment has no available hardware-accelerated emulator/KVM. The user's “where possible” visual-review requirement is met with Compose previews and Robolectric native-graphics renders instead. These are explicitly labeled as host renders; modal dimming is approximated during window compositing. They do not establish final device visual acceptance.
2. **Device-only checks remain outstanding:** actual OS process restart, real IME positioning, TalkBack interaction, physical touch/focus feedback, device rasterization and the complete emulator walkthrough from the specification. Existing persistence/restart unit tests pass, and host tests exercise all four modes, UI corrections and responsive reachability. Owner review is still required before merge.
3. **Responsive scrolling is intentional.** At 200% font or reduced height, controls remain reachable by scrolling. Master history/scratch details can move the leader/secondary Finish affordance below the initial viewport; the primary player/score/shot controls take precedence. No touch targets are reduced below 48 dp.
4. The starting palette was refined for readability, including a darker primary action green. No scoring defaults, feature flags or data semantics changed. Before images cover setup/idle/after-hit; scorecard and history have after renders, and the previous always-visible ball controls are represented by the before-idle frame rather than a nonexistent prior chooser state.

The specification's manual-device and owner visual-acceptance checklist is therefore still open. This commit prepares an implemented, tested design for that review, not approval to merge or ship.

## Files changed/added

- `native-android/README.md`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/MainActivity.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/ui/DesignSystem.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/ui/LiveScreen.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/ui/ReviewPreviews.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/ui/ScorekeeperApp.kt`
- `native-android/app/src/main/java/com/nodetoursstudio/threeball/ui/SetupScreen.kt`
- `native-android/app/src/test/java/com/nodetoursstudio/threeball/GameplayUiTest.kt`
- `native-android/app/src/test/java/com/nodetoursstudio/threeball/UiVisualReviewTest.kt`
- `native-android/docs/UI_DESIGN_SPEC.md`
- `native-android/docs/UI_REDESIGN_REPORT.md`
- `native-android/docs/ui-review/README.md`
- `native-android/docs/ui-review/before-master-after-hit.png`
- `native-android/docs/ui-review/before-master-idle.png`
- `native-android/docs/ui-review/before-setup.png`
- `native-android/docs/ui-review/history.png`
- `native-android/docs/ui-review/master-after-hit.png`
- `native-android/docs/ui-review/master-chooser.png`
- `native-android/docs/ui-review/master-font-200.png`
- `native-android/docs/ui-review/master-idle.png`
- `native-android/docs/ui-review/master-landscape.png`
- `native-android/docs/ui-review/master-short.png`
- `native-android/docs/ui-review/master-tall.png`
- `native-android/docs/ui-review/scorecard.png`
- `native-android/docs/ui-review/setup.png`
