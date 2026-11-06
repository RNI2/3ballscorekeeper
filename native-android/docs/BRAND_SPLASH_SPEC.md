# 3 Ball Scorekeeper | Phase 2.4 Brand Consistency & Visual Fidelity

**Status:** Approved direction; implementation and emulator visual acceptance pending.  
**Base:** `development/native-android-ui-richness` at `153ffa1539b3480be3e181538b3f5241ac8305b4`  
**Proposed branch:** `development/native-android-brand-splash`

## Goal
Improve startup branding and the in-app header without altering the functional Play / History / Settings architecture. Keep the branded experience premium, readable, and consistent. The current system splash shows an awkward small icon inside a white circle; the in-app header uses tiny plain studio text. These should feel like the same product.

## Source-of-truth branding (required)
The three supplied originals in `docs/brand/source/` are **authoritative, unmodified**:
- `No_Detours_Studio_Primary_Logo.png`
- `No_Detours_Studio_Dark_Horizontal.png`
- `No_Detours_Studio_Square_Icon.png`

The white-and-blue **ND arrow mark** and official lettering are the studio identity. Do not invent an orange/gold ND monogram, redraw the arrow, or approximate the official logo with typography. Use supplied image assets or exact source-preserving transformations only. Do not add "LLC" inside the logo artwork unless it already appears there; if needed, typeset "LLC" as a separate, subordinate legal qualifier.

## 3-Ball identity and emblem
Keep the existing red 3-ball brand. For the loading experience and compact in-app header, create or select **one shared, exact 3-Ball emblem asset** that can scale without changing the ball's appearance. The illustrative concept images in `docs/brand/references/` are *not* authoritative logo artwork: they depict different 3-Ball emblems and invented compositions. Do not use their generated ND logo representations in place of the official uploaded assets. If the new complex shield-and-three-ball emblem is not available as a clean standalone production asset, do not extract a blurry crop and call it final. Use the existing crisp red 3-ball icon for now and document a separate approved emblem asset requirement.

## Android startup behavior
- Android 12+ system splash is platform-controlled. Use the same master 3-Ball icon (appropriately scaled, masked, and high-contrast) against a compatible charcoal/emerald `windowSplashScreenBackground`; avoid the current white disk/bad framing if possible. Preserve system splash behavior rather than trying to display a full-screen raster as the system icon.
- If first launch genuinely requires initialization, an optional in-app branded loading surface can show the 3-Ball mark, `3-Ball Scorekeeper`, the **real horizontal No Detours Studio logo**, and a small loading indicator. It must disappear as soon as the app is ready. Do not add a timed fake splash or block quick startup.
- Do not store a splash-specific copy of the 3-Ball logo if a shared asset can be reused.
- Respect edge-to-edge insets, dark status bar/nav bar contrast, motion-reduction, small phones, and API 26+ (including Android 12+ behavior).

## In-app header
- Preserve existing screen structure and the compact header height needed for gameplay.
- Show the same red 3-ball identity and `3-Ball Scorekeeper` title. Replace the plain tiny `NO DETOURS STUDIO LLC` text treatment with a crisp, proportionate **official horizontal studio lockup** or a responsive compact ND mark plus a short studio name if necessary; do not let it dominate the game controls.
- Prefer transparent-asset rendering that keeps original white-and-blue colors. Place logo on dark neutral panel for contrast, not green-on-green.
- On narrow devices, use a compact variant rather than shrinking text to illegibility. Provide content descriptions for meaningful logo use; avoid redundant screen-reader repetition.

## Visual fidelity boundaries
- Existing Phase 2.3 charcoal canvas, emerald hero surfaces, copper/gold highlights, colored actions, and button states are approved as the starting visual system.
- Do not replace app screens with concept art or implement fake mockup controls, invented statistics, or alternative game modes.
- Polish only necessary header / startup visual details and shared brand tokens. Optionally adjust header spacing, alignment, surface contrast, and logo sharpness.
- Keep performance smooth and bitmap memory usage sensible. Supply density-aware resources, inspect launcher icon adaptive layers, and avoid huge bitmap backgrounds in the native system splash.

## Code constraints
Do not change engine, records, Room migrations, DataStore, ViewModel scoring transitions, JSON schemas, feature flags, web `index.html`, or Capacitor. Preserve active-game restoration and stored user data. No Firebase or added services. Do not change application ID.

## Review / acceptance
1. Side-by-side visuals of Android system splash, any genuine in-app loading screen, and Game Setup / Live Scoring / History headers at representative emulator sizes.
2. Official ND logo is pixel-faithful to the supplied originals; 3-Ball brand icon is the **same** in splash and in-app header, subject only to platform size/mask constraints.
3. No white-circle-on-square misframing; no stretched, cut-off, or blurry brand image.
4. Header remains compact enough that core controls remain visible during scoring.
5. Fast app startup has no artificial delay; a returning game still restores correctly.
6. Existing test suite, 16 web parity scenarios, JSON checks, `:app:assembleDebug`, and `:app:lintDebug` pass.
7. Provide `docs/BRAND_SPLASH_REPORT.md` documenting assets chosen, Android system splash limitations, emulator comparisons, tests, deviations, and any decision awaiting owner approval.

Commit on a fresh branch **locally only**; do not push or merge. Preserve all attached original assets unchanged. Visual approval remains pending until the owner views the emulator.
