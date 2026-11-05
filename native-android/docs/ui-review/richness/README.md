# Phase 2.3 visual review

Owner visual approval is pending. These artifacts document the implementation, not final design acceptance.

The approved targets were inspected before implementation: [Setup](../../mockups/game-setup.png), [Live](../../mockups/live-scoring.png), [History](../../mockups/history-results.png), [three-screen composite](../../mockups/approved-three-screen-reference.png), and [detailed Live](../../mockups/live-scoring-detailed-reference.png). See [implementation report](../../UI_RICHNESS_REPORT.md).

## Actual Android emulator comparisons

Unedited Android screencaps, 360 × 800 physical pixels at 160 dpi, Android 9 / API 28 x86_64, software CPU and SwiftShader graphics. Before uses the approved Phase 2.2 APK; after uses this phase's debug APK installed over it with `pm install -r -t`. The app was never uninstalled and its data was never cleared. A cold launch preserved the exact active-game payload and both completed-game payloads. Subsequent scoring and completion used the app's controls, adding test records normally.

System animation scales were zero for stable captures. This AVD has hardware navigation keys, so it does not exercise gesture-navigation insets. Captures include Android's status bar and the app navigation. Software emulation is slow and does not establish physical-device performance or tactile quality. A transient SystemUI ANR during initial cold boot was dismissed with Wait before the walkthrough; the warmed emulator completed the documented app workflow.

| State | Before | After |
| --- | --- | --- |
| Default Setup | ![Before Default Setup](emulator/before/setup.png) | ![After Default Setup](emulator/after/setup.png) |
| Master Setup | ![Before Master Setup](emulator/before/setup-master.png) | ![After Master Setup](emulator/after/setup-master.png) |
| Master zero | ![Before Master zero](emulator/before/master-idle.png) | ![After Master zero](emulator/after/master-idle.png) |
| Master negative score | ![Before Master negative score](emulator/before/master-after-hit.png) | ![After Master negative score](emulator/after/master-after-hit.png) |
| Master chooser | ![Before Master chooser](emulator/before/master-chooser.png) | ![After Master chooser](emulator/after/master-chooser.png) |
| Novice | ![Before Novice](emulator/before/novice.png) | ![After Novice](emulator/after/novice.png) |
| Scorecard / Players | ![Before Scorecard / Players](emulator/before/scorecard.png) | ![After Scorecard / Players](emulator/after/scorecard.png) |
| Collapsed History | ![Before Collapsed History](emulator/before/history.png) | ![After Collapsed History](emulator/after/history.png) |
| Expanded History | ![Before Expanded History](emulator/before/history-expanded.png) | ![After Expanded History](emulator/after/history-expanded.png) |
| History round 2 | ![Before History round 2](emulator/before/history-later-round.png) | ![After History round 2](emulator/after/history-later-round.png) |
| Settings | ![Before Settings](emulator/before/settings.png) | ![After Settings](emulator/after/settings.png) |

The walkthrough uses default Player 1/Player 2 names. Novice has a one-round tie; Master has two completed rounds with negative totals. The later-round view changes presentation only. The restored active Master turn initially has one hit, three pocketed balls and score −2. Undo and re-entry recreate zero/chooser/negative states through the existing commands.

## Reproducible host comparisons

These are Robolectric native-graphics renders of Compose, **not emulator screenshots**. Before uses the approved baseline UI and after uses the new UI with the same deterministic sample data, viewport and font settings. Host modal-root compositing approximates dimming and excludes real system bars, keyboard and device rasterization. A pressed render can match the resting frame because host capture does not reliably include transient native-layer elevation/translation; it is not proof of dynamic feedback.

| State | Before | After |
| --- | --- | --- |
| setup | ![Before setup](before/setup.png) | ![After setup](after/setup.png) |
| setup-master | ![Before setup-master](before/setup-master.png) | ![After setup-master](after/setup-master.png) |
| master-idle | ![Before master-idle](before/master-idle.png) | ![After master-idle](after/master-idle.png) |
| master-after-hit | ![Before master-after-hit](before/master-after-hit.png) | ![After master-after-hit](after/master-after-hit.png) |
| master-chooser | ![Before master-chooser](before/master-chooser.png) | ![After master-chooser](after/master-chooser.png) |
| novice | ![Before novice](before/novice.png) | ![After novice](after/novice.png) |
| scorecard | ![Before scorecard](before/scorecard.png) | ![After scorecard](after/scorecard.png) |
| history | ![Before history](before/history.png) | ![After history](after/history.png) |
| history-expanded | ![Before history-expanded](before/history-expanded.png) | ![After history-expanded](after/history-expanded.png) |
| history-later-round | ![Before history-later-round](before/history-later-round.png) | ![After history-later-round](after/history-later-round.png) |
| settings | ![Before settings](before/settings.png) | ![After settings](after/settings.png) |
| master-short | ![Before master-short](before/master-short.png) | ![After master-short](after/master-short.png) |
| master-tall | ![Before master-tall](before/master-tall.png) | ![After master-tall](after/master-tall.png) |
| master-landscape | ![Before master-landscape](before/master-landscape.png) | ![After master-landscape](after/master-landscape.png) |
| master-font-130 | ![Before master-font-130](before/master-font-130.png) | ![After master-font-130](after/master-font-130.png) |
| master-font-200 | ![Before master-font-200](before/master-font-200.png) | ![After master-font-200](after/master-font-200.png) |
| six-players | ![Before six-players](before/six-players.png) | ![After six-players](after/six-players.png) |
| history-round | ![Before history-round](before/history-round.png) | ![After history-round](after/history-round.png) |
| scorecard-round | ![Before scorecard-round](before/scorecard-round.png) | ![After scorecard-round](after/scorecard-round.png) |
| hit-pressed | ![Before hit-pressed](before/hit-pressed.png) | ![After hit-pressed](after/hit-pressed.png) |

Normal host viewport is 360 × 800 dp. Additional cases cover 360 × 640, 400 × 900, landscape, 130%/200% fonts, a six-player roster and overflowing round strips. Existing Compose previews remain available in `ReviewPreviews.kt`.

## Comparison with approved targets

- Live uses a neutral charcoal canvas, local emerald hero, serif player name, large tabular signed score, small billiard motif, neutral stat tiles, green Hit, separate confirmation-green Done, slate Undo/Back, blue Scorecard and red Finish. Copper is concentrated on the hero rather than every container.
- Setup retains the native stacked difficulty cards with decorative gold emblems, explicit check selections, blue Add Player and green Start. Options use two columns when the available width permits; narrow phones and larger fonts stack the complete native labels instead of copying abbreviated mockup fields.
- History uses compact player-score summaries when collapsed and neutral rows/gold winner accents when expanded. Existing one-expanded-game and selectable-round behavior remains intact.
- Mockup sample names, scores, winner claims, extra filters and navigation labels are illustrative. Actual lowest-score eligibility and existing Play / History / Settings organization take precedence. No photo-dependent imagery or external fonts were added.
- Physical-device haptics, TalkBack reading order, real IME/keyboard occlusion, modern gesture-navigation/edge-to-edge insets, and device performance still require owner/device review. Host responsive coverage is not equivalent to those checks.
