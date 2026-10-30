# Rules extracted from the web reference

Reference: repository `index.html`, commit `03cc3d5` (BUILD `live-score-history-swipe-20261021`). Reviewed before implementing the engine. Only this HTML is present in the checkout; `www/` and a Capacitor Android project are absent. No existing application files were changed.

## Four modes

| Mode | Physical shots per turn | Score | Players |
| --- | --- | --- | --- |
| Novice | Unlimited | hits + scratches | 2–6 |
| Pro | Configurable integer 1–99, required | hits + scratches | 2–6 |
| Expert | 5 | hits + scratches | 2–6 |
| Master | Configurable integer 1–99; default 6 | hits − pocketed balls + scratches + remaining penalty | 2–20 |

Lowest accumulated score wins; ties remain ties. Scores are summed across every saved turn, including saved entries in partial rounds. The unfinished draft does not count. Master can score below zero: clearing three balls on the break scores −2; adding a scratch makes it −1. Do not clamp scores to zero.

### Standard modes: Novice, Pro, Expert

Source anchors: `shotCap`, `physicalShots`, `standardHit`, `standardUndo`, `scratch`, `done`.

- Each HIT, including the break, adds one physical shot and one point.
- Scratch adds one point without consuming a physical shot. The UI permits scratches before any HIT and multiple scratches before the cap. This behavior is preserved, even though scratches usually accompany a shot in real play.
- At the shot cap, one additional scratch is permitted. A second is blocked until Undo Last Hit resets the final-scratch latch. Previously recorded scratches remain when undoing a standard hit.
- Standard HIT and Scratch reject increments when the draft score is already 999. Novice has no physical shot cap, but shares this input ceiling.
- Done is permitted whenever the draft score is at least one; there is no pocketed-ball tracking or requirement to reach the cap. A scratch-only turn can therefore be saved.
- Undo Last Hit removes one physical shot and one point; it does not remove a scratch. The web also has hidden legacy plus/minus controls; these are not part of the initial native API.

### Master

Source anchors: `masterScore`, `masterShot`, `masterAddScratch`, `masterUndo`, `done`.

- Each HIT records an integer ball outcome from zero through the number of balls remaining, initially three. One HIT may pocket multiple balls.
- Every HIT contributes one point; every pocketed ball subtracts one point.
- Add Scratch attaches exactly one penalty point to the latest HIT. It requires a HIT and cannot be repeated on that same HIT. It remains available after a clear or after the cap, before Done.
- At the cap, if the table is not cleared, add one point per remaining ball automatically. Clearing at the cap incurs no remaining-ball penalty.
- Done requires either all three balls pocketed or the cap reached. Negative and zero turns are valid.
- Undo removes the most recent HIT, all its pocketed balls, and its attached scratch. The remaining-ball penalty is recomputed from the resulting turn.
- Optional round limit: blank means unlimited; otherwise an integer 1–999. After the last active player's turn in the final round, enter review; do not automatically archive. Explicit Finish Game saves the result.

### Scratch placement

Ball in Hand means cue ball anywhere; Kitchen means behind the head string. A choice is required at setup. This is recorded as game metadata and does not change arithmetic in any mode.

## Players, navigation, and finishing

Source anchors: `activeIdx`, `totals`, `completedRoundCount`, `archiveGame`, `back`, `reopenMasterTurn`, `data-withdraw` handler, `gameAction`.

- Names default to Player N; names can be changed during a game. Adding players is setup-only. Native setup supplies the full name list to `start`; an interactive roster editor is deferred.
- Done saves the draft, clears draft counters, and advances to the next active player. The final active player advances the round, except at a Master final-round review boundary.
- Back moves to the previous active player, crossing the round boundary as needed, and reopens their saved score. At the first position it stays there. A dirty draft requires explicit discard confirmation from the UI.
- Master scorecard corrections reopen the complete per-HIT breakdown, clear that saved entry while editing, and return to the original position and phase after Done. Back is blocked while doing such a correction.
- The web Back handler from final review resets to the first active player, then applies its normal backward-navigation logic. This can select the previous round rather than the last turn in the final round. The native initial engine preserves that behavior.
- Withdrawal is available only while more than two players are active. Preserve scores but exclude withdrawn players from winners and future turns. Mark the 1-based withdrawal round. If the current player withdraws, the visible web handler discards their draft and selects the first active player in the same round; it does not automatically advance to the next player. Native matches that handler, rather than the separate legacy `removePlayer` helper.
- A round is complete only if every player required for that round has a saved score. A withdrawal exempts the player starting with their withdrawal round; it does not excuse blank scores in earlier rounds. Setup-inactive players are exempt in the web implementation.
- Finish before any complete round produces no history entry. Otherwise preserve all saved score rows, including partial rounds, and totals/scratches. Finishing discards an unsaved draft. Abandon always discards the game without history.

## Reference discrepancies and native scope decisions

1. The live `masterScore()` and rules dialog use **physical hits − pocketed balls + scratches + remaining**. A `masterScoreFormulaV3` migration instead uses **zero-ball hits − pocketed balls + scratches + remaining**. Native follows live gameplay. No legacy localStorage import or score migration is included; importing existing web history must address this explicitly.
2. The web render path has an inconsistent comparison involving `masterScore() + ballsLeft`, but assigns `masterScore()` after setting the remaining-ball count. Native derives the penalty once, without render-time mutations.
3. Native rejects withdrawal and Finish during a Master scorecard correction so a temporarily cleared saved entry cannot be archived or stranded. This guard is an intentional tightening of the web's loose controls and must be reflected in the future UI.
4. Manual standard scorecard edits (web accepts blank or 1–99), legacy hit representations, and legacy score migrations are deferred. Native-generated Master records always contain per-hit outcomes.
5. Engine commands reject invalid actions with `IllegalArgumentException` rather than silently ignoring them as web click handlers do. Future UI should disable invalid controls and translate validation failures into appropriate feedback.

## Verification

`engine/src/test/resources/scoring-vectors.tsv` supplies 16 shared scenarios. Kotlin tests check those scores and counters. `tools/verify-web-reference.mjs` reads the unmodified HTML and executes its actual scoring handlers in a Node VM against the same scenarios, including Done eligibility. It does not load the app, migrations, or DOM. Additional JUnit tests cover rules and state transitions; this is not a claim of exhaustive equivalence for every web UI path.
