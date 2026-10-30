package com.nodetoursstudio.threeball.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.nodetoursstudio.threeball.GameUiState
import com.nodetoursstudio.threeball.engine.*
import com.nodetoursstudio.threeball.records.*

internal data class ReviewRequest(val type: String, val target: Position? = null, val name: String = "")

/** Presentation-only availability projection. Every mutation still goes through the existing ViewModel. */
internal fun actionAllowed(game: GameState, action: Action): Boolean = runCatching {
    when (action.type) {
        "HIT" -> GameEngine.hit(game, action.balls ?: 0)
        "SCRATCH" -> GameEngine.scratch(game)
        "UNDO" -> GameEngine.undoHit(game)
        "UNDO_SCRATCH" -> GameEngine.undoScratch(game)
        "DONE" -> GameEngine.done(game)
        "BACK" -> GameEngine.back(game, true)
        "FINISH" -> GameEngine.finish(game, 0L)
        else -> error("Unknown UI action")
    }
}.isSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LiveScreen(ui: GameUiState, send: (Action) -> Unit, abandon: () -> Unit, initialChooser: Boolean = false, initialScorecard: Boolean = false) {
    val game = requireNotNull(ui.game)
    val record = requireNotNull(ui.active)
    var scorecard by rememberSaveable { mutableStateOf(initialScorecard) }
    var chooser by remember { mutableStateOf(initialChooser) }
    var log by rememberSaveable { mutableStateOf(false) }
    var request by remember { mutableStateOf<ReviewRequest?>(null) }
    val largeType = LocalDensity.current.fontScale > 1.4f
    val master = game.config.difficulty == Difficulty.MASTER
    val review = game.phase == GamePhase.REVIEW
    fun enabled(action: Action) = !ui.busy && actionAllowed(game, action)
    if (scorecard) {
        BackHandler { scorecard = false }
        ScorecardScreen(game, ui.busy, { scorecard = false }) { request = it }
    } else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(TableSpace.Gutter), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("${game.config.difficulty.title()}  ·  ${game.config.placement.title()}", fontSize = 12.sp, color = TableColors.Muted)
        RailSection(if (review) "FINAL REVIEW" else "CURRENT TURN") {
            RoundProgress(game)
            if (game.editing != null) Text("Correcting a saved turn · Done returns to play", color = TableColors.Gold, fontSize = 12.sp)
            if (review) Text("Final round complete — review scores and finish.", color = TableColors.Gold)
            PlayerScoreCard(if (review) "Final scores" else game.players[game.position.player].name,
                if (review) game.totals[game.position.player] else game.draft.score(game.config))
            if (!review) {
                if (largeType) Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatTile("SHOTS", "${game.draft.shots}${game.config.shotCap?.let { "/$it" } ?: ""}", Modifier.fillMaxWidth())
                    if (master) StatTile("POCKETED", "${game.draft.pocketed}/3", Modifier.fillMaxWidth())
                    StatTile("SCRATCHES", "${game.draft.scratches}", Modifier.fillMaxWidth())
                } else Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatTile("SHOTS", "${game.draft.shots}${game.config.shotCap?.let { "/$it" } ?: ""}", Modifier.weight(1f))
                    if (master) StatTile("POCKETED", "${game.draft.pocketed}/3", Modifier.weight(1f))
                    StatTile("SCRATCHES", "${game.draft.scratches}", Modifier.weight(1f))
                }
                if (master) {
                    Text("${3 - game.draft.pocketed} balls remaining" + if (game.draft.remainingPenalty(game.config) > 0) " · Penalty +${game.draft.remainingPenalty(game.config)}" else "", color = TableColors.Gold, fontSize = 12.sp)
                    if (game.draft.hits.isNotEmpty()) {
                        SecondaryActionButton(if (log) "Hide hit history" else "Hit history (${game.draft.hits.size}) · latest ${game.draft.hits.last().balls} pocketed", { log = !log }, modifier = Modifier.fillMaxWidth())
                        if (log) HitLog(game.draft)
                    }
                }
                val hit = Action("HIT", 0)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryScoreButton(if (master) "Hit" else "HIT +1", { if (master) chooser = true else send(hit) }, enabled = enabled(hit), modifier = Modifier.weight(1f))
                    SecondaryActionButton("Undo Last Hit", { send(Action("UNDO")) }, enabled = enabled(Action("UNDO")), modifier = Modifier.weight(1f))
                }
                // Master scratch is shown only for an eligible latest hit; standard counters remain independent.
                if (!master || actionAllowed(game, Action("SCRATCH"))) {
                    SecondaryActionButton(if (master) "Scratch last hit +1" else "Scratch +1", { send(Action("SCRATCH")) }, enabled = enabled(Action("SCRATCH")), modifier = Modifier.fillMaxWidth())
                } else if (game.draft.hits.lastOrNull()?.scratch == true) Text("✓ Scratch recorded on last hit", color = TableColors.Gold, fontSize = 12.sp)
                if (!master && game.draft.scratches > 0) SecondaryActionButton("Undo Scratch", { send(Action("UNDO_SCRATCH")) }, enabled = enabled(Action("UNDO_SCRATCH")), modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryActionButton("Back", { if (game.draft.hasEntries) request = ReviewRequest("back") else send(Action("BACK")) }, enabled = enabled(Action("BACK")), modifier = Modifier.weight(1f))
                    PrimaryScoreButton("Done →", { send(Action("DONE")) }, enabled = enabled(Action("DONE")), modifier = Modifier.weight(1f))
                }
                val hint = when {
                    game.config.shotCap != null && game.draft.shots >= game.config.shotCap!! -> "Shot limit reached · undo or complete this turn."
                    master && game.draft.pocketed == 3 -> "Table cleared · add a scratch if needed, then Done."
                    !actionAllowed(game, Action("DONE")) -> if (master) "Clear the table or reach the shot cap before Done." else "Record a hit or scratch before Done."
                    else -> null
                }
                hint?.let { Text(it, fontSize = 12.sp, color = TableColors.Muted) }
            }
        }
        LeaderPreview(game)
        SecondaryActionButton("Scorecard / Players", { scorecard = true }, modifier = Modifier.fillMaxWidth())
        TextButton(onClick = { request = ReviewRequest("finish") }, enabled = enabled(Action("FINISH")), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Finish Game", color = if (enabled(Action("FINISH"))) TableColors.Gold else TableColors.Muted) }
    }
    if (chooser) ModalBottomSheet(onDismissRequest = { chooser = false }, containerColor = TableColors.Felt) {
        Column(Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("How many balls went in?", style = MaterialTheme.typography.titleLarge)
            Text("${3 - game.draft.pocketed} balls remaining · choose to record one hit", color = TableColors.Muted)
            (0..(3 - game.draft.pocketed)).toList().chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { balls -> PrimaryScoreButton("$balls", { chooser = false; send(Action("HIT", balls)) }, enabled = enabled(Action("HIT", balls)), modifier = Modifier.weight(1f).semantics { contentDescription = "$balls balls pocketed" }) }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            SecondaryActionButton("Cancel", { chooser = false }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }
    }
    request?.let { current -> GameConfirmation(game, record, current, !ui.busy, { request = null }, { action ->
        if (action.type == "REOPEN") scorecard = false
        send(action)
    }, abandon) }
}

@Composable
internal fun RoundProgress(game: GameState) {
    val position = game.activeIndices.indexOf(game.position.player) + 1
    Text("Round ${game.position.round + 1}${game.config.roundLimit?.let { "/$it" } ?: ""} · Player $position of ${game.activeIndices.size}", color = TableColors.Muted, fontSize = 12.sp)
}
@Composable
internal fun LeaderPreview(game: GameState) {
    val leaders = game.winnerIndices
    RailSection("CURRENT LEADER") {
        if (leaders.isEmpty()) Text("Your table is ready. First saved turn sets the pace.", fontSize = 13.sp, color = TableColors.Muted)
        else {
            Text((if (leaders.size > 1) "Tie · " else "") + leaders.take(2).joinToString { game.players[it].name } + if (leaders.size > 2) " +${leaders.size - 2} tied" else "", fontWeight = FontWeight.Bold)
            Text("${game.totals[leaders.first()]} points · lowest saved total", color = TableColors.Gold, fontSize = 12.sp)
        }
    }
}
@Composable
internal fun HitLog(turn: Turn) {
    turn.hits.forEachIndexed { i, hit -> Text("Hit ${i + 1} · ${hit.balls} pocketed${if (hit.scratch) " · Scratch +1" else ""}", fontSize = 13.sp) }
}

@Composable
private fun ScorecardScreen(game: GameState, busy: Boolean, close: () -> Unit, request: (ReviewRequest) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(TableSpace.Gutter), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryActionButton("Return to scoring", close, modifier = Modifier.fillMaxWidth())
        RailSection("SCORECARD / PLAYERS") {
            Text("Saved totals", style = MaterialTheme.typography.titleLarge)
            game.players.forEachIndexed { i, player ->
                val leader = i in game.winnerIndices
                PlayerRosterCard("${player.name} · ${if (game.rounds.any { it[i] != null }) game.totals[i].toString() else "—"}",
                    (if (leader) "LEADER · " else "") + if (i == game.position.player) "CURRENT PLAYER" else "PLAYER ${i + 1}", leader) {
                    if (!player.active) Text("Withdrawn · round ${player.withdrawnRound}", color = TableColors.Muted)
                    SecondaryActionButton("Rename ${player.name}", { request(ReviewRequest("rename", Position(game.position.round, i), player.name)) }, enabled = !busy, modifier = Modifier.fillMaxWidth())
                    if (player.active && game.activeIndices.size > 2) DestructiveActionButton("Withdraw ${player.name}", { request(ReviewRequest("withdraw", Position(game.position.round, i))) }, enabled = !busy && game.editing == null, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        game.rounds.forEachIndexed { r, row ->
            if (row.any { it != null }) RailSection("ROUND ${r + 1}") {
                row.forEachIndexed { player, turn ->
                    if (turn != null) {
                        Text("${game.players[player].name} · ${turn.score(game.config)} points", fontWeight = FontWeight.Bold)
                        Text("${turn.shots} shots · ${turn.scratches} scratches" + if (game.config.difficulty == Difficulty.MASTER) " · ${turn.pocketed} pocketed" else "", fontSize = 12.sp, color = TableColors.Muted)
                        if (game.config.difficulty == Difficulty.MASTER) {
                            HitLog(turn)
                            SecondaryActionButton("Edit ${game.players[player].name} · round ${r + 1}", { request(ReviewRequest("reopen", Position(r, player))) }, enabled = !busy && game.editing == null && turn.hits.isNotEmpty(), modifier = Modifier.fillMaxWidth())
                            if (turn.hits.isEmpty()) Text("No individual hits available to edit.", color = TableColors.Muted)
                        }
                    } else Text("${game.players[player].name} · —", color = TableColors.Muted)
                }
            }
        }
        if (game.config.difficulty != Difficulty.MASTER) Text("Use Back on the scoring screen to reopen a previous turn.", color = TableColors.Muted)
        DestructiveActionButton("Abandon without saving to history", { request(ReviewRequest("abandon")) }, enabled = !busy, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun GameConfirmation(game: GameState, record: GameRecord, request: ReviewRequest, enabled: Boolean,
                             dismiss: () -> Unit, send: (Action) -> Unit, abandon: () -> Unit) {
    var name by remember(request) { mutableStateOf(request.name) }
    AlertDialog(onDismissRequest = dismiss, containerColor = TableColors.Felt, title = { Text(when (request.type) {
        "finish" -> "Finish this game?"; "abandon" -> "Abandon this game?"; "rename" -> "Rename player"; "withdraw" -> "Withdraw player?"; else -> "Correct a turn?"
    }, style = MaterialTheme.typography.titleLarge) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (request.type == "rename") OutlinedTextField(name, { name = it }, singleLine = true, label = { Text("Display name") })
            else Text(when (request.type) {
                "finish" -> if (game.completedRoundCount == 0) "No complete rounds yet. End without adding this game to history?" else "Save final results on this device? Any unfinished turn is discarded."
                "abandon" -> "Discard this game without adding it to history?"
                "withdraw" -> "Scores are preserved, but this player cannot win. Withdrawing the current player discards their unfinished turn."
                else -> if (game.draft.hasEntries) "Discard the unfinished turn and reopen the selected turn?" else "Reopen this saved turn for correction?"
            })
            if (request.type == "finish" && game.completedRoundCount > 0) {
                Text("${if (game.winnerIndices.size > 1) "Tie" else "Winner"}: " + game.winnerIndices.joinToString { game.players[it].name }, color = TableColors.Gold)
                game.players.forEachIndexed { i, p -> Text("${p.name}: ${game.totals[i]}${if (!p.active) " (withdrawn)" else ""}") }
            }
        }
    }, confirmButton = { TextButton(enabled = enabled, onClick = {
        val target = request.target
        when (request.type) {
            "abandon" -> abandon()
            "finish" -> send(Action("FINISH"))
            "back" -> send(Action("BACK", discardDraft = true))
            "rename" -> if (target != null) send(Action("RENAME", targetPlayerId = record.players[target.player].playerId, name = name))
            "withdraw" -> if (target != null) send(Action("WITHDRAW", targetPlayerId = record.players[target.player].playerId))
            "reopen" -> if (target != null) send(Action("REOPEN", targetPlayerId = record.players[target.player].playerId, targetRound = target.round, discardDraft = true))
        }
        dismiss()
    }) { Text("Confirm") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}
