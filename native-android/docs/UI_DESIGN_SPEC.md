# 3 Ball Scorekeeper — Native Mobile UI/UX Design Specification

**Document status:** Approved design direction; implementation specification for Phase 2.1  
**Owner:** No Detours Studio LLC  
**Target:** Android Kotlin + Jetpack Compose; reusable visual/interaction reference for iOS  
**Baseline:** `RNI2/3ballscorekeeper`, branch `development/native-android-phase2`, commit `ce7917ac6605fb8f64b447d659eaa8ab1156584d`  
**Intended repository path:** `native-android/docs/UI_DESIGN_SPEC.md`  
**Scope:** UI and interaction presentation only. Preserve tested engine, persistence, and data contracts.

## 1. Product vision

**The app should feel like a premium digital pool-table scoreboard, not a generic Android form or a long scrolling website.** Bring the original web app's rich green-felt, dark wood/copper framing, cream typography, red 3-ball identity, and teal cards into a compact native mobile layout. The primary job is rapid and reliable score entry at a real pool table.

Design principles, in priority order:

1. **Fast scoring:** Critical controls are one tap away, large, and unambiguous.
2. **Current turn first:** Player name, score, round, and scoring actions dominate the Play screen.
3. **Recognizable identity:** Faithfully adapt the web visual language without replicating its one-page structure.
4. **Predictable feedback:** A hit, scratch, undo, correction, and end-of-turn visibly update the right metrics.
5. **Accessible and resilient:** Support larger fonts, small devices, landscape, and app restoration without obscuring actions.
6. **Cross-platform clarity:** Android and iOS may use different native controls but must share labels, state semantics, and design tokens where practical.

## 2. Source of truth and reference material

- **Visual reference:** User-provided web screenshots in the Phase 2.1 design discussion: (a) Game Setup with four stacked difficulty cards and Ball in Hand / Kitchen selectors; (b) Live Score Totals, Add Player, Scorecard, Game History; (c) Master Current Turn before a hit, during the 0–3 pocketed-ball choice, after hit and scratch availability, and completed leaderboard/scorecard.
- **Visual implementation reference:** Repository root `index.html` (CSS and markup). Inspect its actual design tokens and arrangements; screenshots represent visual intent, not fixed pixel layouts.
- **Functional authority:** `native-android/engine/` and documented `native-android/docs/SCORING_RULES.md`. Do not infer or alter rules based on screenshots.
- **Persistence/data authority:** `native-android/records/`, Room repository, `native-android/docs/IOS_DATA_CONTRACT.md`, and golden contract fixtures.
- **Current native UI:** `native-android/app/src/main/java/com/nodetoursstudio/threeball/MainActivity.kt` and related UI code.

The screenshots show multiple responsive browser widths, **not approved Android pixel-perfect wireframes**. Do not copy browser scrollbar, permanent multi-section page stack, or screen height directly.

## 3. Visual system

### 3.1 Core palette (initial tokens)

| Token | Starting value | Intended usage |
| --- | --- | --- |
| `feltDeep` | `#021F1C` | Deepest interior and shadows |
| `feltBase` | `#07352D` | App background (matches current native reference) |
| `feltRaised` | `#104C3F` | Cards and elevated surfaces |
| `feltTeal` | `#125548` | Player/selection panels; refine from original CSS |
| `cream` | `#FFF8E8` | Primary text, serif section titles |
| `gold` | `#F0D28B` | Score highlights, rules indicators, separators |
| `copperRail` | `#7D3B18` | Framed panel outlines / wood rail impression |
| `greenAction` | `#168D5D` | Primary action emphasis |
| `blueAction` | `#1684DC` | Secondary player-management accent |
| `redAction` | `#B52323` | Destructive / finish visual treatment (with confirmation) |

These are **starting design tokens, not a demand for exact raw colors**. Inspect `index.html` and screenshots before finalizing. Use coherent light/dark contrast and ensure enabled/disabled states differ visibly. Avoid flat bright green screens or overuse of gold fills.

### 3.2 Materials and styling

- Deep, subtly textured felt look. Prefer lightweight Compose gradients/patterns over network-fetched backgrounds. Do not make the interface depend on Unsplash or any remote image.
- Sections should evoke **pool table rails**: rounded corners, restrained dark copper/wood border, fine gold separator under small uppercase serif section title.
- Player cards use a restrained teal gradient and slim colored accent bar (blue for neutral player; green for the current leader when earned). Never rely on color alone to identify winner or current player.
- Buttons should read as tactile and responsive, with clear pressed, focused, selected, enabled, and disabled states. Use shadows sparingly.
- Use the existing red 3-ball logo/vector as a recognizable branded accent in header and/or primary Hit button, without overwhelming the controls.
- Serif is for brand and section headings; use a legible sans-serif for stats, controls, editable text, and small labels. Prefer bundled/system fonts, not new font subscriptions.

### 3.3 Spacing and responsiveness

- Respect Android status/navigation bars, keyboard insets, gesture navigation, and device cutouts.
- Minimum interactive touch target **48 dp** (prefer 52–56 dp for core scoring actions).
- Typical outer gutters 12–16 dp; coherent gaps 6–12 dp; avoid accidental giant empty areas.
- Design primarily for a phone around **360–400 dp wide** and typical phone height; test smaller heights and 200% font scaling. No text may be clipped or overlap controls.
- Keep **active player, score, key stats, and Hit/Undo/Done** visible together at ordinary phone size where practical. If the viewport cannot fit safely, allow scrolling instead of shrinking controls below usable touch sizes.
- For many players (2–6 ordinarily; Master up to 20 according to current native rules), show a compact leader preview; full roster lives in a separate sheet/screen.

## 4. Navigation and information architecture

Persistent main destinations:

1. **Play**: Setup if no active game; otherwise Live Scoring.
2. **History**: Completed local games, with round details.
3. **Settings**: Available local settings such as keep screen awake.

Use a compact, branded native bottom navigation bar with icon + label and a clearly selected destination. Hide feature-gated Accounts/Cloud/Leaderboards. Do not add empty destinations. Preserve existing navigation semantics and safe access to an active game.

**Secondary views**, invoked within Play, rather than permanently stacked below scoring:

- **Scorecard / Players**: Modal bottom sheet or dedicated sub-screen for standings, detailed round scorecard, renaming, withdrawing, and saved-turn corrections. Keep keyboard safe.
- **Game finish/final review**: Confirmation with scores and winner/tie information, using existing engine constraints.
- **Hit choice**: Inline expanded action region or modal sheet for Master pocketed-ball choice, whichever gives the clearest single-hand action and avoids layout jumps.
- **Rules**: Compact informative modal/sheet; don't turn Setup into a rules encyclopedia.

If a sheet would obscure critical score controls or break accessibility, use a dedicated sub-screen instead. Android Back should dismiss the current sheet/dialog before navigating away; it must **not silently invoke the game's score-correction Back command**.

## 5. Screen specifications

### 5.1 Game Setup

**Visual order:** branded header → framed **GAME SETUP** section → four difficulty cards → contextual mode fields → scratch-placement selection → player roster → primary **Start Game**.

- **Difficulty cards:** Four full-width rows, in order Novice, Pro, Expert, Master. Show title, short descriptive subtitle, compact **Rules** affordance, and a strong selected treatment. Cards resemble the web screenshot's textured emerald horizontal panels, not small default filter chips.
- **Mode fields:** Only show inputs needed by the selected mode; preserve engine-validated caps and round limits. Make errors explanatory and inline. Never change scoring defaults merely for visual convenience.
- **Scratch placement:** Two distinct selectable cards, **Ball in Hand** and **Kitchen**, with one-sentence definitions and obvious selected state.
- **Players:** Two visible initial names; add/remove and edit support; obey per-mode maximums in the current engine. On narrow screens stack controls instead of cutting labels.
- **Primary CTA:** Branded **Start Game**, disabled only for real validation/busy reasons. Keyboard should not cover it; allow normal scroll on Setup.
- **Rules details:** Present as a sheet/dialog accessible from each difficulty. Do not hard-code new rules that differ from `SCORING_RULES.md`.

### 5.2 Live Scoring (highest priority)

**Normal compact layout, top to bottom:**

1. Small branded app bar (logo and title) and concise difficulty / scratch placement metadata.
2. Framed **CURRENT TURN** with round progress and position (e.g., `Round 1/3 · Player 1 of 2`).
3. Large current-player card: player name left, prominent **SCORE** and numeric value right. Support negative Master scores without clipping.
4. Three compact stat tiles **SHOTS**, **POCKETED**, **SCRATCHES** for Master; only applicable metrics for other modes. Show `hits taken / cap` where applicable.
5. Compact, expandable hit history summary and balls remaining when relevant.
6. Primary action cluster: **Hit**, **Undo Last Hit**, scratch control when valid, **Back** (game correction) and **Done** (advance/end turn). Prioritize frequent actions and align labels with existing engine commands.
7. Compact **Current Leader** preview with score; accessible **Scorecard / Players** control for full details.
8. Deliberately secondary **Finish Game** affordance with confirmation; keep it from competing visually with Hit.

**Important:** Buttons must reflect valid engine actions and disabled reasons. Never display a clickable action that silently does nothing. Keep all current functionality: corrections, withdrawal, name changes, final review, game completion, abandon confirmation, and local restoration.

### 5.3 Master-mode hit interaction states

Reference screenshots show a deliberate flow; preserve its meaning:

- **Idle:** Show Hit and Undo; available scratch actions reflect state.
- **Hit tapped:** Reveal **How many balls went in?** with only *valid* choices 0, 1, 2, 3 as permitted by the remaining balls. Use 2×2 large buttons (or equivalent accessible layout) and Cancel. Do not record a hit merely by opening the chooser.
- **After selection:** Immediately show updated score, shots, balls pocketed, balls remaining, and hit-history summary; the scratch-for-last-hit affordance appears only when permitted by the engine.
- **Undo:** Correctly removes the last hit and any attached scratch according to verified engine rules; reflect this immediately in UI.
- **At cap / final review:** Show remaining-ball penalty and eligibility constraints from engine; no premature finish or accidental loss of correction state.
- **Negative score:** Display e.g. `-2` clearly in player score and leaderboard. Do not treat negative values as an error.

**Non-Master modes** retain the existing single-hit and independent scratch/undo semantics. Do not impose Master UI on them.

### 5.4 Scorecard / Players

- Not a permanently visible full-width table on the Play screen.
- Open from a clear **Scorecard / Players** button; present full round-by-round values, names, current player, and totals.
- Keep saved-turn corrections, rename/withdraw controls and appropriate warning dialogs discoverable. Never allow UI to bypass engine state guards.
- Show 2–20 players with horizontal/vertical scrolling as needed and clear frozen/visible labels; use accessible alternatives to complex data tables when possible.
- Preserve per-hit / round details when available; indicate when older data lacks editable hit detail instead of inventing it.

### 5.5 History

- Dedicated **History** destination with branded framing, completed-game card summary (date, mode, winner/tie, total/round information), and drill-down for round details.
- Empty state should feel intentional and friendly, not like a blank developer placeholder.
- Respect storage/recovery warnings without discarding unrecognized data. Avoid presenting casual results as verified competitive achievements.

### 5.6 Settings

- Styled grouped panels consistent with the game; keep screen awake preference and any already-implemented local options.
- No fake account, sync, leaderboard, or export controls. Disabled features remain hidden behind existing flags.

## 6. Component design system (Compose)

Extract reusable presentation components rather than scattering one-off styling throughout `MainActivity.kt`:

- `ThreeBallTheme` / design token objects (colors, typography, spacing, shapes).
- `BrandedHeader` with red three-ball icon and concise title.
- `RailSection` for copper-framed sections with cream serif title and gold separator.
- `DifficultyCard` and `ScratchRuleCard` with accessible selected state.
- `PlayerScoreCard`, `StatTile`, `LeaderPreview`, and `RoundProgress`.
- `PrimaryScoreButton`, `SecondaryActionButton`, `DestructiveActionButton`, with consistent feedback and disabled visuals.
- `ThreeBallBottomBar` with labeled destinations.
- Reusable dialogs/sheets for hit choice, scorecard, rules, and destructive confirmations.

Prefer Compose drawing, gradients, vectors and existing project resources. No network-based artwork, substantial binary assets, web views, new backend services, or paid libraries.

## 7. Behavior and safety invariants (non-negotiable)

- **DO NOT CHANGE** tested `:engine` scoring rules, Master correction semantics, `:records` contracts, Room database/migrations, JSON schema or feature flags as part of visual work.
- Preserve app ID `com.nodetoursstudio.threeball` and existing debug-build capability.
- UI uses existing `GameViewModel`/StateFlow and its public command flow, not new independent scoring calculations.
- Preserve save-before-publish behavior and app restart recovery.
- Keep local results casual and offline. No Firebase, authentication, HTTP permissions, region collection, synchronization, or leaderboards in this phase.
- Preserve confirmation flows for abandon, finish, dirty Back, and corrections.
- Respect engine conditions for disabling actions; ensure no double tap causes duplicate scoring.
- Do not edit root `index.html`, web/Capacitor projects, or branches other than the isolated redesign branch.

## 8. Accessibility and quality

- Test portrait at 360×800 dp, a taller phone, a smaller usable height, and large font scaling; verify no clipped negative scores or obstructed action buttons.
- Logical TalkBack labels for ball-choice buttons, score changes, player identities, icon-only actions, and current selection. Don't encode meaning by color alone.
- Keyboard/IME must not hide editable player names, confirmation actions, or selected text.
- Contrast should be legible on deep green; cream/gold for emphasis and descriptive labels legible at small sizes.
- Provide clear error/loading/recovery states, and preserve settings/history access during normal flow.
- Avoid animations that delay repeated score entry. Optional subtle feedback must not mutate engine state.

## 9. Acceptance criteria / definition of done

**Visual:**

- [ ] Original web identity is immediately recognizable (red three-ball, dark felt, framed panels, cream/gold headings, teal surfaces).
- [ ] No generic flat Material form as the main visual presentation.
- [ ] Setup offers full-width difficulty cards, contextual configuration, scratch selector, player management, and an obvious Start Game.
- [ ] Live Scoring presents current player/score, metrics, primary actions and leader preview clearly at typical phone size; scrolling is minimized without sacrificing touch target sizes.
- [ ] Master Hit chooser, scratch, undo, Done, corrections, and negative scores have distinct readable states.
- [ ] Scorecard and History no longer permanently occupy the Play screen.
- [ ] Bottom navigation is branded and clear; dialogs and back behavior are consistent.

**Functional/regression:**

- [ ] All existing JVM engine, records, Room, DataStore, ViewModel, and Compose tests pass.
- [ ] All JavaScript parity scenarios and portable JSON contract checks pass.
- [ ] Debug APK assembles and lint has no newly introduced errors/warnings.
- [ ] Manual emulator walkthrough covers Novice, Pro, Expert, Master; hit/undo/scratch; turn advancement; score correction; abandon/finish; history; process restart/recovery.
- [ ] No edits outside `native-android/`; no web, scoring/data contract, Firebase, or production changes.
- [ ] Before/after Android emulator screenshots for Setup, Master idle/hit chooser/after hit, leaderboard/scorecard, and History are supplied for design review.

**Completion note:** Automated tests passing does *not* establish visual acceptance. Owner reviews emulator screenshots and gameplay before any branch merge.

## 10. Implementation and Git workflow

1. Start at `development/native-android-phase2` commit `ce7917ac6605fb8f64b447d659eaa8ab1156584d`.
2. Create separate branch `development/native-android-ui-redesign` (or equivalent isolated name).
3. Before coding, review the original `index.html` visual rules and this specification. Preserve current business logic; identify files to change.
4. Implement theme/components and screen changes in small coherent steps. Keep Compose components testable and previewable.
5. Run the current test matrix, `:app:assembleDebug`, `:app:lintDebug`, web parity and JSON contract validators.
6. Document what changed, any deviations, and unverified device states. Provide emulator screenshots when possible.
7. Commit on the isolated branch; **do not push or merge without approval**.

## 11. Phase 2.1 exclusions

Not in scope: Firebase setup, authentication, cloud sync, regional/global leaderboards, monetization, ads, legacy web data import, server-side verification, new scoring features, major engine refactor, app store signing, or introducing new paid services.

---

**Design approval:** Visual direction agreed with owner based on the existing web screenshots. Detailed native implementation and final screenshots remain subject to owner review.
