# 3 Ball Scorekeeper | Native Android UI Polish Specification

**Document version:** 1.0  
**Project:** No Detours Studio LLC — 3 Ball Scorekeeper  
**Workstream:** Phase 2.2, Native UI Polish  
**Baseline:** `development/native-android-ui-redesign` at `f04873d7bded804f66e9ee610abb818583282265`  
**Target branch:** `development/native-android-ui-polish`  
**Status:** Approved design direction; implementation and visual acceptance pending

## 1. Goal and non-goals

Make the existing native Kotlin/Jetpack Compose app look and feel like a finely crafted billiards scorekeeper. Preserve the original web app's deep emerald felt, copper/mahogany framing, cream serif headings, red 3-ball identity, and legible teal panels. Make interactive controls tactile, with dimensional surfaces and immediate, restrained feedback.

**This is a visual-and-interaction polish pass, not a structural rewrite.** Preserve the existing **Play / History / Settings** navigation, dedicated scorecard access, and compact scoring layout. Preserve all functional scoring modes, game rules, persistent data, history, identifiers, feature flags, and iOS JSON contracts. Do not add Firebase, accounts, monetization, network services, or new gameplay features.

## 2. Visual reference and hierarchy

**Canonical visual reference:** existing repository `index.html` and the owner's supplied iPhone captures of the web app: Game Setup; Current Turn in Master mode before/after hits; hit-pocketed chooser; Live Score Totals; Scorecard; and expanded multi-round Game History. Use these as the reference for visual identity and information treatment, **not** as a requirement to put all sections on one scrolling page.

**Priority:** (1) Active player and live score, (2) primary scoring action, (3) hits/pocketed/scratches/balls remaining, (4) undo/scratch/done, (5) leader preview, (6) scorecard and finish access. Current-turn controls should be visible without routine vertical scrolling on a typical phone; on small screens, landscape, and large fonts, enable natural, accessible scrolling rather than clipping.

### Color tokens (starting values; adjust cautiously for contrast)

| Token | Value | Usage |
| --- | --- | --- |
| `feltDeep` | `#032F28` | Main dark background |
| `feltBase` | `#07352D` | Screen and section surfaces |
| `feltRaised` | `#105448` | Elevated cards, difficulty rows |
| `feltPressed` | `#087453` | Selected / pressed green |
| `copperRail` | `#8B451F` | Distinctive, restrained thick framing |
| `copperHighlight` | `#B77643` | Rail edge highlights |
| `warmGold` | `#F0D28B` | Emphasized labels and premium accents |
| `ivory` | `#FFF8E8` | Primary text and scoreboard numbers |
| `mutedText` | `#C6DCD4` | Secondary descriptions |
| `blueAction` | `#075B8A` | Add Player and supportive actions |
| `greenAction` | `#0A8A5A` | Main Hit action |
| `dangerRed` | `#B52323` | Abandon/destructive actions |

Do not make every border glow or every label gold. Use genuine contrast, depth, and purposeful accents. Subtle vertical felt-stripe or grain effects are acceptable if lightweight, self-contained, and do not hurt legibility. No remote background images or online asset dependencies.

### Typography and brand

- Use a **cream serif** for app title and section headings (akin to the web design), with accessible fallback fonts. Use high-legibility sans-serif for metrics, buttons, labels and table data.
- The red glossy **3-ball** icon should remain central to branding, with subtle highlight and shadow; implement with local vector/drawable/Compose art, not a runtime web image.
- Keep score numerals particularly prominent, using tabular figures where supported. Negative scores, ties, zero, and long player names must render cleanly.
- Align labels, scores, stat tiles and buttons on a consistent grid with predictable padding.

## 3. Reusable Compose design system

Implement themed, reusable building blocks (names are suggestions):

- `BilliardsTheme` with tokens for colors, type, shapes, elevation and spacing.
- `RailSection` for mahogany/copper-framed sections with a thin highlighted edge and refined heading strip.
- `FeltCard` and `PlayerScoreCard` for green raised panels, winner/leader state and secondary statistics.
- `TactileActionButton` for primary, secondary, destructive and disabled states.
- `StatTile` for shots, pocketed, scratches and balls remaining.
- `BrandedHeader` and icon-bearing bottom navigation with a strong active state.
- `CompactStandings` and `RoundSummary` reused across play and history.

Prefer shallow gradients, highlights and elevation over excessive shadows. Touch targets should be at least 48 dp; primary Hit controls preferably 56–64 dp or larger. Components must support font scaling, TalkBack labels and loading/disabled semantics.

## 4. Tactile behavior and micro-interactions

- Primary scoring buttons visually depress on pointer/touch down through a tiny scale/elevation change, then spring/release quickly. Aim for roughly 80–150 ms feedback, respecting system reduced-motion settings where available.
- Use a restrained highlight sweep or border emphasis for selection; do **not** add persistent pulsing/neon effects.
- Provide haptic feedback for a **successfully accepted** Hit, Scratch and Done action where supported and allowed by accessibility/device settings. Avoid haptics for disabled actions and avoid duplicate haptics on recomposition.
- The displayed score may animate between values subtly, but updates must remain immediate and numerically accurate; no deferred persistence or tap-blocking animations.
- Selected difficulty/scratch-rule cards have a visible border and filled state, not color alone.
- Disabled buttons are distinct and readable, without implying they can be pressed. Error, save-in-progress, and destructive-confirmation states are clearly differentiated.
- Rapid successive taps must never generate accidental duplicate inputs from animation or interaction state. Engine/ViewModel remains the only source of truth.

## 5. Screen-level acceptance targets

### 5.1 Game Setup

- Retain the four stacked, rich teal difficulty cards with name, explanatory subtitle and Rules affordance. Strong selected treatment, inset texture, and restrained dimensionality.
- Clear scratch-placement choice: Ball in Hand vs Kitchen, each as equal-weight cards with descriptions and visible selection state.
- Group conditional mode settings and player entries into visually distinct compact sections. Make add/remove/rename controls easy to identify.
- Primary **Start Game** becomes the natural next step, with clear enabled state. Never obscure invalid configuration or player validation.
- Avoid multiple near-identical nested cards or unnecessarily tall blank gaps.

### 5.2 Live Scoring (highest priority)

- Distinct copper-framed Current Turn region with **round / player position / scratch rule** as concise context.
- Current-player panel: large name on left, dramatic high-contrast score on right. Player name truncates/wraps safely, without overlapping score.
- Three compact, aligned stat tiles: **Shots**, **Pocketed** (Master), **Scratches**; include hits remaining/balls remaining when relevant.
- Prominent, tactile **Hit** button anchored in the interaction hierarchy. **Undo** and **Scratch** are visually secondary but always easy to reach when valid. **Done** is distinct, and **Back** remains accessible.
- For Master, tapping Hit opens the existing pocketed-ball chooser as a clearly styled **0 / 1 / 2 / 3** selection state (only valid counts enabled). Preserve corrections, scratch-to-last-hit, final review and the actual existing engine logic.
- Hit history is a compact expandable summary, not a large always-open log.
- Small current standings preview displays the leader and up to a practical number of players with a clear **View Scorecard / Players** affordance. Do not bring a full scorecard back onto the main scoring screen.
- Finish and Abandon use deliberate, clearly differentiated controls and retain existing confirmations.

### 5.3 Scorecard / Player management

- Preserve the current secondary screen/sheet behavior; do not force full scorecard content into Live Scoring.
- Improve round headings, selected round states, score-cell contrast, player distinction and leader/winner emphasis.
- Ensure corrections, renaming and withdrawals remain accessible and use clear destructive/confirmation patterns.

### 5.4 History

- First level: visually rich but compact game cards with date, difficulty, winner/tie and result snapshot.
- Expand/select a game to see rankings, score columns, round navigation, and Master hit details. Avoid presenting multiple expanded games by default.
- Retain negative scores, medals, SCR, AVG, BEST, and round/event details accurately, with horizontal scroll for wide tables when needed rather than unreadably compressed columns.
- No account or online ranking controls while feature flags are disabled.

### 5.5 Settings and navigation

- Preserve **Play / History / Settings** bottom navigation, but add consistent icons and selected-state hierarchy, with comfortable bottom safe-area spacing.
- Settings uses the same framing and type system without decorative overload. Preserve keep-screen-awake settings and offline-only communication.
- Do not change navigation semantics or lose active-game state when switching tabs.

## 6. Layout, responsiveness and accessibility

- Design for a representative 360–420 dp wide portrait phone. Avoid hard-coded pixel sizes and ensure essential scoring controls fit comfortably at default font scale and standard height.
- Verify smaller screens, 1.3×–2× font scaling, longer player names, 2-player and 6-player scenarios and Master configurations.
- Respect status/navigation bars, display cutouts, on-screen keyboard and orientation changes; scrolling is allowed when required for accessibility.
- Maintain sufficient text contrast (aim at WCAG AA where practical), avoid color-only success indicators, provide meaningful content descriptions and logical focus order.
- Keep performance smooth, lightweight, and usable offline; no internet-dependent images/fonts/textures.

## 7. Development boundaries and protection

- Work exclusively under `native-android/`, starting from `f04873d7...`; create `development/native-android-ui-polish` separately.
- Do not alter `index.html`, the web assets, Capacitor, application ID, scoring formulas, engine rule transitions, Room persistence/migrations, feature flags or platform-neutral iOS JSON schema.
- Keep existing regression tests; add Compose UI tests for core navigation, enabled/disabled states, Master chooser, safe layout rendering and interaction feedback where useful.
- Do not change game history semantics or backfill/migrate existing data in this visual pass.
- If a functional defect is discovered, document and flag it rather than silently expanding scope.
- Commit on the polish branch **locally only** until approved. No push, merge or release.

## 8. Evidence and sign-off criteria

Codex must provide:

1. Files changed and a focused design-system/component inventory.
2. Host-render or Compose-preview before/after comparisons for **Setup**, **Live Scoring** (Novice and Master, including chooser), **Scorecard**, **History** and **Settings**.
3. A small gallery or index under `native-android/docs/ui-review/` referencing preview evidence, plus actual layout screenshots when emulator testing is available.
4. Existing tests, additional UI test results, Android debug assembly and lint outputs; explicitly distinguish host preview from device/emulator acceptance.
5. A written report `native-android/docs/UI_POLISH_REPORT.md` with accomplishments, deviations, limitations and branch/commit.

**Owner acceptance in Android Studio is required before this phase is considered visually complete.** Compare against the supplied web screenshots. The decisive question: does the UI preserve the elegant original billiards identity while feeling tactile and unmistakably native, without making a user scroll to record each shot?

## 9. Follow-on handoff

Once approved, reuse the visual tokens, component behavior, and interaction states as the reference for the independent iOS implementation. Do not make the iOS app depend on Kotlin internals; share behavior, visual specs and the existing portable game-record contract.
