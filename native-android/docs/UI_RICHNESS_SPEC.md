# 3 Ball Scorekeeper | Phase 2.3 UI Richness Specification

**Document ID:** NDS-3BALL-UI-023  
**Version:** 1.1 (approved visual mockup addendum)  
**Status:** Visual direction and mockups approved; implementation and emulator acceptance pending  
**Owner:** No Detours Studio LLC  
**Base branch:** `development/native-android-ui-polish`  
**Base commit:** `1e5e1d67c7cc5bdbc5a9bb852e299ff9406f17bc`  
**New branch:** `development/native-android-ui-richness`  
**Repository destination:** `native-android/docs/UI_RICHNESS_SPEC.md`

## 1. Objective

Elevate the Phase 2.2 native Android interface from overly monochromatic dark green to a **modern premium billiards scorekeeper**. Keep the Play / History / Settings screens, scoring workflow, and approved layouts. The existing web UI and user-supplied iPhone screenshots are **brand references**, not layouts to replicate. Achieve modern contrast, clean material separation and satisfying touch controls without a dim, smoky bar atmosphere.

## 2. Scope and safeguards

**In scope:** Compose theme/design tokens, backgrounds, borders, typography presentation, stat cards, button surfaces/pressed states, small spacing refinements, navigation visual treatment and accessibility/contrast polish.

**Out of scope:** Any change to scoring engine, game rules, ViewModel command semantics, Room/DataStore, migrations, JSON contracts, feature flags, accounts, Firebase, history calculations, navigation architecture, or gameplay flow. Do not change `index.html`, `www/`, Capacitor, or files outside `native-android/`. Do not uninstall the app or reset saved data.

All changes must remain UI-focused. If a non-visual change is strictly necessary, flag it separately and seek approval rather than silently implementing it.

## 3. Visual direction: four distinct layers

1. **App canvas:** Near-black charcoal/dark slate, with a slight cool teal undertone. **Not full-screen green felt**.
2. **Section containers:** Raised neutral charcoal/slate with selective fine copper framing and subtle separation shadows.
3. **Felt surfaces:** Rich emerald green inside current-player hero cards, selected difficulty cards, and selected game-related panels. Felt texture is restrained and local, never a full-screen haze.
4. **Interactive controls:** Visually distinct, dimensional green primary, blue utility, charcoal neutral, red destructive/terminal, and gold selected/winner states.

**Avoid:** blanket copper outlines, murky green-on-green backgrounds, exaggerated woodgrain, casino neon, excessive glow, high-cost blur/shaders, and gimmicky continuous animations. Target the polish of premium billiards equipment, not an old tavern or a slot machine.

## 4. Proposed semantic color tokens

These are starting points to evaluate in emulator screenshots, not rigid constraints if accessibility requires tuning.

| Token | Suggested hex | Purpose |
|---|---|---|
| Canvas | `#121B1E` | Primary neutral background |
| Elevated canvas | `#1B2A2E` | Nav, secondary containers |
| Panel | `#183135` | Section interiors |
| Deep felt | `#093F36` | Felt depth/shadow |
| Felt | `#0E5C4E` | Hero/player and selected game surfaces |
| Felt highlight | `#16816A` | Controlled highlights |
| Copper | `#B66D43` | Thin branded framing |
| Muted copper | `#754A38` | Subtle borders/dividers |
| Warm ivory | `#F7F2E7` | Main text and scores |
| Secondary text | `#BDD0CD` | Help labels |
| Gold | `#E8BE67` | Leaders, winners, selections |
| Primary green | `#12845C` | Hit / Start Game |
| Green highlight | `#21A879` | Raised button edge |
| Utility blue | `#176D9D` | Scorecard / Add Player |
| Blue highlight | `#319FD0` | Raised blue edge |
| Destructive red | `#B63839` | Abandon / destructive controls; consider separately styled Finish |
| Neutral action | `#31454A` | Undo, Back, Cancel |
| Disabled | `#344244` | Unavailable controls |

Implement reusable semantic tokens in the Compose design system; avoid hardcoding scattered colors.

## 5. Brand and typography

- Preserve the glossy **red 3-ball logo**, cream serif display/section headings, and modern sans-serif body text.
- Player name and score are the dominant typographic pair in Live Scoring; use tabular figures for scores.
- Make stat labels readable and distinct from their values. Negative Master scores must be clear.
- Distinguish winner/leader with gold **and** label/icon, never color alone.
- Avoid tiny labels, crowded text, and truncation of long names at ordinary font scales.

## 6. Physical, purposeful buttons

| Action | Style |
|---|---|
| Hit / Start Game | Emerald primary, glossy top edge, pressed depth |
| Done | Distinct green confirmation/progression style, clearly different from Hit |
| Undo / Back / Cancel | Neutral slate with obvious enabled/disabled state |
| Add Player / Scorecard | Rich blue utility action |
| Finish Game / Abandon | Intentional high-contrast terminal/destructive treatment with existing confirmations |
| Selected mode / scratch rule / round | Gold/copper border + visible check/indicator |

Pressed state: subtle ~0.98–0.99 scaling/translation, reduced shadow, altered highlight, quick ~80–140 ms response; release restores depth. Do not delay rapid taps. **Accepted-action haptics must occur only after successful persistence**, not merely on touch-down; no success haptic for rejected/failed/disabled actions. Respect system reduced-motion and accessibility settings. Touch targets aim for at least 48dp.

## 7. Screen-specific requirements

### 7.1 Game Setup

- Retain stacked Novice/Pro/Expert/Master cards and current Rules affordances.
- Put cards on distinct neutral page background; use intentional teal/felt surfaces *inside* cards.
- Selected mode is unmistakable through contrasting border, check, glow/highlight, not a barely different shade of green.
- Give GAME SETUP, GAME OPTIONS, AFTER A SCRATCH, PLAYERS clear hierarchy without making every section equally visually heavy.
- Clean, readable inputs with conspicuous focus and validation; polished scratch-rule choice tiles.
- Blue Add Player and primary green Start Game must stand out. Setup scrolling is acceptable.

### 7.2 Live Scoring (highest priority)

- Maintain current native arrangement and Play navigation.
- Current player hero card should contrast with the parent canvas and make the score prominent.
- Shots/Pocketed/Scratches sit in three aligned, visually separated stat tiles.
- Hit has the strongest visual weight; Undo and Back are neutral; Done is distinct and legible even disabled.
- Keep scoring controls high enough that a normal ~360x800dp device can record a routine turn without unnecessary scrolling.
- Compact standings visibly distinguish leaders and support negative scores, ties and unplayed values.
- Scorecard/Players remains accessible as its existing secondary view/sheet, not a permanent dashboard beneath the scorer.
- Finish and Abandon actions remain available with their existing confirmations.

### 7.3 Master hit chooser

- Preserve current bottom-sheet interaction and available 0/1/2/3 ball options based on remaining balls.
- Use a neutral charcoal/slate sheet, emerald option tiles and clearly differentiated Cancel button.
- Heading, remaining-ball count, and option states should read immediately; preserve the existing scoring and save behavior.

### 7.4 Scorecard / Players

- Keep existing access and correction flow.
- Favor neutral row backgrounds and crisp separators; don't fill the entire table with similar greens.
- Clear editing selection, player identities, round labels and withdrawal affordances. Support large rosters without unreadable shrinking.

### 7.5 History

- Keep one expanded game at a time, selected-round controls and derived SCR/AVG/BEST values.
- Collapsed cards show date, mode, winner(s) and concise score summary; details are progressively disclosed.
- Expanded tables use distinct rows and gold winner highlighting, with clear selected-round state.
- Do not modify saved results when changing displayed round; maintain usable strips for long games.

### 7.6 Settings / bottom navigation

- Neutral settings cards; maintain keep-screen-awake preference.
- Bottom bar gets a neutral dark base with an obvious active tab pill, well-contrasted icons and labels.
- Coordinate system status bar/navigation bar colors and safe-area insets; no clipped controls.

## 8. Accessibility, performance, review targets

- Test at **360x800dp** and a larger phone, normal and ~1.3x font scale; check keyboard, edge-to-edge, landscape/short windows and TalkBack order.
- Preserve readable contrast and disabled state visibility. Functional hierarchy outranks decorative detail.
- Avoid performance-heavy textures/blur and incessant animation.
- Produce **same-state, same-size** before/after visual renders for: Setup unselected, Master setup selected, live Master at zero, live Master after scoring a negative value, Master chooser, Novice scoring, scorecard/player sheet, collapsed/expanded History with later round, Settings, and nav active states.
- Report which renders are Robolectric/host previews versus actual emulator captures.

### Visual acceptance checklist

- [ ] Neutral canvas visibly differs from felt and framed panels.
- [ ] The player/score hero stands out at a glance.
- [ ] Buttons have distinct semantic colors and tactile pressed/disabled states.
- [ ] Copper/gold are selective accents, not blanket treatments.
- [ ] Selected difficulty, scratch rule, round and tab are unmistakable.
- [ ] Routine scoring fits a normal phone without needless scrolling.
- [ ] All existing automated tests, JavaScript parity, JSON checks, debug assembly and lint pass.
- [ ] Manual emulator review completed; **visual owner approval still required before merge**.

## 9. Implementation deliverables

1. Create `development/native-android-ui-richness` from Phase 2.2 commit `1e5e1d67c7cc5bdbc5a9bb852e299ff9406f17bc`.
2. Save this specification **unchanged** under `native-android/docs/UI_RICHNESS_SPEC.md`.
3. Implement visual adjustments using reusable Compose design tokens/components.
4. Add or update UI tests without altering scoring/storage semantics.
5. Produce side-by-side review renders/previews and `native-android/docs/UI_RICHNESS_REPORT.md` (changes by screen, tests, lint, build, previews, deviations, manual-check gaps).
6. Commit **locally only**; do **not** push or merge without explicit permission.
7. Report full commit SHA, branch, clean/dirty working tree, test results and any exceptions.

## 10. Cross-platform guidance

This specification provides the visual design language that the independent iOS developer may adapt to native iOS patterns. It does not alter or supersede the shared gameplay rules or `IOS_DATA_CONTRACT.md`.


## 11. Approved visual mockups (Phase 2.3 visual acceptance target)

The owner approved the visual direction represented by the following images. Codex **must inspect the actual PNG files** before implementation, not rely on text descriptions alone. Images are illustrations, not executable specifications.

| Reference file | Scope | Primary decisions to reproduce |
|---|---|---|
| [`mockups/live-scoring.png`](mockups/live-scoring.png) | Main Play screen | Charcoal canvas, emerald hero, brass/copper fine edges, rich gold scoring accents, blue utility and red Finish, tactile buttons and compact standings |
| [`mockups/game-setup.png`](mockups/game-setup.png) | Setup | Distinct beveled difficulty rows, clear gold selection, two-column game options, scratch rule tiles, standout Start Game |
| [`mockups/history-results.png`](mockups/history-results.png) | History | Compact collapsed games, only one expanded, winner highlighting, round-detail entry point, readable score rows |
| [`mockups/approved-three-screen-reference.png`](mockups/approved-three-screen-reference.png) | Cross-screen composite | Consistent brand and visual continuity across Play, Setup, History |
| [`mockups/live-scoring-detailed-reference.png`](mockups/live-scoring-detailed-reference.png) | Detailed Live Scoring visual | Strong score hierarchy, pronounced primary/secondary buttons, isolated standings and Scorecard action |

### Mockup interpretation rules

- **Mandatory visual traits:** premium clear contrast, near-black/charcoal page surfaces, selected emerald-felt cards, warm ivory serif headings, subtle copper/gold trim, signature glossy red 3-ball, and distinctly raised button surfaces.
- **Keep the approved native screens and functional navigation:** Play, History and Settings; separate Scorecard/Players access; Master chooser. Do not turn native app back into a single long scrolling website.
- **Behavior comes from existing Kotlin rules, not images.** The mockups may depict illustrative or contradictory scores, winner labels, player counts, hit icons, disabled states, and completion controls. Never implement these literally. Example: a hypothetical History image may label a higher scoring player the winner, but the actual app must continue to award the win to the *lowest eligible total*.
- **The image is a design target, not a demand for pixel-perfect rendering.** Device safe areas, dynamic type, screen dimensions, accessibility, and Compose-native layout take priority.
- **Avoid excessive glossy ornamentation.** Use the mockups' contrast and hierarchy, but keep the native app crisp, performant and legible. No photo-dependent backgrounds or external assets unless licensed and packaged.
- Current-player details and Hit/Undo/Back/Done must remain readily available at 360×800 dp; scroll is acceptable for Setup and long History.
- Provide actual screenshots from the Android emulator at the same interaction states as these references for visual approval; host renders and Compose previews alone are not acceptance.

### Review sequence

1. Implement reusable tokens and component depth.
2. Prioritize **Live Scoring**, then **Setup**, then **History**, followed by Settings and bottom navigation.
3. Compare each emulator screenshot with the corresponding mockup, noting visual differences without changing game logic.
4. Run current tests, web parity, schema checks, debug build and lint; visual owner approval remains mandatory.
