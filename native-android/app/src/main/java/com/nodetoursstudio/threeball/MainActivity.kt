package com.nodetoursstudio.threeball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nodetoursstudio.threeball.engine.*
import com.nodetoursstudio.threeball.records.*
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ThreeBallApplication
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = GameViewModel(app.games, app.preferences) as T
        }
        setContent { val model: GameViewModel = viewModel(factory = factory); ThreeBallApp(model) }
    }
}

@Composable
internal fun ThreeBallApp(model: GameViewModel) {
    val ui by model.ui.collectAsStateWithLifecycle()
    var page by rememberSaveable { mutableStateOf("Play") }
    val view = LocalView.current
    DisposableEffect(view, ui.active != null, ui.preferences.keepScreenAwake) {
        view.keepScreenOn = ui.active != null && ui.preferences.keepScreenAwake
        onDispose { view.keepScreenOn = false }
    }
    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFF0D28B), secondary = Color(0xFF39D879),
        background = Color(0xFF07352D), surface = Color(0xFF104C3F), onBackground = Color(0xFFFFF8E8), onSurface = Color(0xFFFFF8E8))) {
        Scaffold(bottomBar = {
            Row(Modifier.fillMaxWidth().navigationBarsPadding(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOfNotNull("Play", "History".takeIf { Features.localHistory }, "Settings").forEach { label ->
                    TextButton(onClick = { page = label }) { Text(if (page == label) "• $label" else label) }
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
                if (ui.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                ui.error?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error)
                    Row { TextButton(onClick = model::reload) { Text("Reload saved game") }; TextButton(onClick = model::dismissError) { Text("Dismiss") } }
                }
                if (ui.recoveredRecords > 0) Text("Unreadable local records have been retained for recovery. See History.", style = MaterialTheme.typography.bodySmall)
                if (!ui.loaded) {
                    Text("Opening local games…")
                    if (!ui.busy) Button(onClick = model::reload) { Text("Retry") }
                } else when (page) {
                    "History" -> HistoryScreen(ui.history, ui.recoveredRecords)
                    "Settings" -> SettingsScreen(ui, model)
                    else -> if (ui.active == null) SetupScreen(ui.busy) { config, names -> model.start(config, names) }
                        else LiveScreen(ui, model)
                }
            }
        }
    }
}

@Composable
private fun SetupScreen(busy: Boolean, start: (GameConfig, List<String>) -> Unit) {
    var modeName by rememberSaveable { mutableStateOf(Difficulty.NOVICE.name) }
    val mode = Difficulty.valueOf(modeName)
    var cap by rememberSaveable { mutableStateOf("6") }
    var rounds by rememberSaveable { mutableStateOf("") }
    var placementName by rememberSaveable { mutableStateOf(ScratchPlacement.BALL_IN_HAND.name) }
    var names by rememberSaveable { mutableStateOf(listOf("", "")) }
    val config = runCatching { GameConfig(mode, ScratchPlacement.valueOf(placementName),
        if (mode in listOf(Difficulty.PRO, Difficulty.MASTER)) cap.toIntOrNull() else null,
        if (mode == Difficulty.MASTER && rounds.isNotBlank()) requireNotNull(rounds.toIntOrNull()) else null) }.getOrNull()
    val maxPlayers = if (mode == Difficulty.MASTER) 20 else 6
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Game Setup", style = MaterialTheme.typography.headlineSmall)
        Difficulty.entries.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { pair.forEach { option ->
                FilterChip(selected = mode == option, onClick = { modeName = option.name; cap = if (option == Difficulty.MASTER) "6" else "5" },
                    label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) }, enabled = !busy)
            } }
        }
        Text(when (mode) {
            Difficulty.NOVICE -> "Unlimited hits. Each hit and scratch adds 1 point."
            Difficulty.PRO -> "Choose a hit limit. Each hit and scratch adds 1 point."
            Difficulty.EXPERT -> "Five-hit limit. Each hit and scratch adds 1 point."
            Difficulty.MASTER -> "Hits − pocketed balls + scratches + balls left at the cap."
        })
        if (mode in listOf(Difficulty.PRO, Difficulty.MASTER)) OutlinedTextField(cap, { cap = it }, label = { Text("Hits per turn (1–99)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
        if (mode == Difficulty.MASTER) OutlinedTextField(rounds, { rounds = it }, label = { Text("Rounds (1–999, blank = unlimited)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
        Text("Scratch placement")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScratchPlacement.entries.forEach { value -> FilterChip(placementName == value.name, { placementName = value.name },
                label = { Text(if (value == ScratchPlacement.KITCHEN) "Kitchen" else "Ball in Hand") }, enabled = !busy) }
        }
        Text(if (placementName == "KITCHEN") "Cue ball behind the head string." else "Cue ball anywhere after a scratch.")
        names.forEachIndexed { index, name ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { value -> names = names.mapIndexed { i, old -> if (i == index) value else old } },
                    label = { Text("Player ${index + 1}") }, placeholder = { Text("Player ${index + 1}") }, singleLine = true,
                    enabled = !busy, modifier = Modifier.weight(1f))
                if (names.size > 2) TextButton(onClick = { names = names.filterIndexed { i, _ -> i != index } }, enabled = !busy) { Text("Remove") }
            }
        }
        OutlinedButton(onClick = { names = names + "" }, enabled = !busy && names.size < maxPlayers, modifier = Modifier.heightIn(min = 48.dp)) { Text("Add player") }
        if (config == null) Text("Enter a valid hit or round limit.", color = MaterialTheme.colorScheme.error)
        if (names.size > maxPlayers) Text("This mode allows up to $maxPlayers players. Remove players or choose Master.", color = MaterialTheme.colorScheme.error)
        Button(onClick = { config?.let { start(it, names) } }, enabled = Features.localScoring && !busy && config != null && names.size <= maxPlayers,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Start Game") }
        Text(stringResource(R.string.studio_name) + " • Saved on this device", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LiveScreen(ui: GameUiState, model: GameViewModel) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val scroll = maxHeight < 520.dp || LocalDensity.current.fontScale > 1.2f
        LiveScreenContent(ui, model, scroll)
    }
}

@Composable
private fun LiveScreenContent(ui: GameUiState, model: GameViewModel, scrolling: Boolean) {
    val record = requireNotNull(ui.active)
    val game = requireNotNull(ui.game)
    var dialog by remember { mutableStateOf<String?>(null) }
    var target by remember { mutableStateOf<Position?>(null) }
    var rename by remember { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    fun enabled(action: Action): Boolean = !ui.busy && runCatching {
        when (action.type) {
            "HIT" -> GameEngine.hit(game, action.balls ?: 0)
            "SCRATCH" -> GameEngine.scratch(game)
            "UNDO" -> GameEngine.undoHit(game)
            "UNDO_SCRATCH" -> GameEngine.undoScratch(game)
            "DONE" -> GameEngine.done(game)
            else -> game
        }
    }.isSuccess
    val current = game.players[game.position.player]
    val master = game.config.difficulty == Difficulty.MASTER
    val review = game.phase == GamePhase.REVIEW
    // The main scoring controls stay above the scrollable scorecard.
    Column(if (scrolling) Modifier.verticalScroll(rememberScrollState()) else Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("${game.config.difficulty.name} • Round ${game.position.round + 1}${game.config.roundLimit?.let { " / $it" } ?: ""} • ${if (game.config.placement == ScratchPlacement.KITCHEN) "Kitchen" else "Ball in Hand"}",
            style = MaterialTheme.typography.bodySmall)
        if (game.editing != null) Text("Correcting a saved turn. Done returns to live play.", color = MaterialTheme.colorScheme.primary)
        if (review) Text("Final round complete — review scores and finish.", color = MaterialTheme.colorScheme.primary)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(if (review) "Final review" else current.name, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            Text("${game.draft.score(game.config)}", style = MaterialTheme.typography.displaySmall)
        }
        Text("Hits ${game.draft.shots}${game.config.shotCap?.let { " / $it" } ?: ""}  •  Scratches ${game.draft.scratches}" +
            if (master) "  •  Pocketed ${game.draft.pocketed} / 3" else "")
        if (game.draft.remainingPenalty(game.config) > 0) Text("Remaining-ball penalty +${game.draft.remainingPenalty(game.config)}", color = MaterialTheme.colorScheme.primary)
        if (!review) {
            if (master) {
                Text("HIT — select balls pocketed", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0..(3 - game.draft.pocketed)).forEach { balls -> val action = Action("HIT", balls)
                        Button(onClick = { model.command(action) }, enabled = enabled(action), modifier = Modifier.weight(1f).heightIn(min = 52.dp), contentPadding = PaddingValues(4.dp)) { Text("$balls") }
                    }
                }
            } else {
                val action = Action("HIT", 0)
                Button(onClick = { model.command(action) }, enabled = enabled(action), modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("HIT +1") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val scratch = Action("SCRATCH"); val undo = Action("UNDO")
                OutlinedButton(onClick = { model.command(scratch) }, enabled = enabled(scratch), modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text(if (master) "Scratch last hit +1" else "Scratch +1") }
                OutlinedButton(onClick = { model.command(undo) }, enabled = enabled(undo), modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Undo Last Hit") }
            }
            if (!master) TextButton(onClick = { model.command(Action("UNDO_SCRATCH")) }, enabled = enabled(Action("UNDO_SCRATCH"))) { Text("Undo Scratch") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { if (game.draft.hasEntries) dialog = "back" else model.command(Action("BACK")) },
                    enabled = !ui.busy && game.editing == null, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Back") }
                Button(onClick = { model.command(Action("DONE")) }, enabled = enabled(Action("DONE")), modifier = Modifier.weight(1f).heightIn(min = 52.dp)) { Text("Done →") }
            }
        }
        Text("Leaderboard • lowest score wins", style = MaterialTheme.typography.labelLarge)
        val sorted = game.players.indices.sortedWith(compareBy<Int> { !game.players[it].active }
            .thenBy { p -> game.rounds.none { it[p] != null } }.thenBy { game.totals[it] })
        Text(sorted.take(3).joinToString("  ·  ") { "${game.players[it].name}: ${if (game.rounds.any { row -> row[it] != null }) game.totals[it].toString() else "—"}${if (!game.players[it].active) " †" else ""}" }, maxLines = 2)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Hide scorecard" else "Scorecard / players") }
            TextButton(onClick = { dialog = "finish" }, enabled = !ui.busy && game.editing == null) { Text("Finish Game") }
        }
        if (expanded || review) LazyColumn(if (scrolling) Modifier.height(400.dp) else Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(game.players) { index, player ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${player.name}: ${game.totals[index]}${if (!player.active) " (withdrawn)" else ""}", modifier = Modifier.weight(1f))
                    TextButton(onClick = { target = Position(game.position.round, index); rename = player.name; dialog = "rename" }, enabled = !ui.busy) { Text("Rename") }
                    if (player.active && game.activeIndices.size > 2) TextButton(onClick = { target = Position(game.position.round, index); dialog = "withdraw" }, enabled = !ui.busy && game.editing == null) { Text("Withdraw") }
                }
            }
            itemsIndexed(game.rounds) { r, row ->
                Text("Round ${r + 1}", style = MaterialTheme.typography.titleSmall)
                row.forEachIndexed { player, turn -> if (turn != null) {
                    val caption = "${game.players[player].name}: ${turn.score(game.config)}" + if (master) " • ${turn.shots} hits, ${turn.pocketed} pocketed, ${turn.scratches} scratch" else " • ${turn.scratches} scratch"
                    if (master) TextButton(onClick = { target = Position(r, player); dialog = "reopen" }, enabled = !ui.busy && game.editing == null) { Text("$caption • Edit") }
                    else Text(caption)
                } }
            }
            if (master && game.draft.hits.isNotEmpty()) item {
                Text("Current hit log: " + game.draft.hits.mapIndexed { i, h -> "${i + 1}: ${h.balls} balls${if (h.scratch) " + scratch" else ""}" }.joinToString(" · "))
            }
            item { TextButton(onClick = { dialog = "abandon" }, enabled = !ui.busy) { Text("Abandon without saving to history") } }
        }
        if (!scrolling && !expanded && !review) Spacer(Modifier.weight(1f))
    }
    dialog?.let { kind ->
        AlertDialog(onDismissRequest = { dialog = null }, title = { Text(when (kind) {
            "finish" -> "Finish this game?"; "abandon" -> "Abandon this game?"; "rename" -> "Rename player"; "withdraw" -> "Withdraw player?"; else -> "Correct a turn?"
        }) }, text = {
            if (kind == "rename") OutlinedTextField(rename, { rename = it }, singleLine = true, label = { Text("Display name") })
            else Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(when (kind) {
                "finish" -> if (game.completedRoundCount == 0) "There are no complete rounds. This game will end without a history record." else "Save final results on this device? Any unfinished turn is discarded."
                "abandon" -> "Discard this game without adding it to history?"
                "withdraw" -> "Recorded scores stay in the scorecard. This player will not be eligible to win. Withdrawing the current player discards their unfinished turn."
                else -> if (game.draft.hasEntries) "Discard the unfinished turn and reopen the selected turn?" else "Reopen this turn for correction?"
                })
                if (kind == "finish" && game.completedRoundCount > 0) {
                    Text("${if (game.winnerIndices.size > 1) "Tie" else "Winner"}: " + game.winnerIndices.joinToString { game.players[it].name })
                    game.players.forEachIndexed { i, player -> Text("${player.name}: ${game.totals[i]}${if (!player.active) " (withdrawn)" else ""}") }
                }
            }
        }, confirmButton = { TextButton(onClick = {
            when (kind) {
                "finish" -> model.command(Action("FINISH"))
                "abandon" -> model.abandon()
                "back" -> model.command(Action("BACK", discardDraft = true))
                "reopen" -> target?.let { model.command(Action("REOPEN", targetPlayerId = record.players[it.player].playerId, targetRound = it.round, discardDraft = true)) }
                "withdraw" -> target?.let { model.command(Action("WITHDRAW", targetPlayerId = record.players[it.player].playerId)) }
                "rename" -> target?.let { model.command(Action("RENAME", targetPlayerId = record.players[it.player].playerId, name = rename)) }
            }
            dialog = null
        }, enabled = !ui.busy) { Text("Confirm") } }, dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancel") } })
    }
}

@Composable
private fun HistoryScreen(history: List<GameRecord>, recovered: Int) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Game History", style = MaterialTheme.typography.headlineSmall) }
        if (recovered > 0) item { Text("$recovered unreadable record(s) preserved for recovery. Valid games remain available.") }
        if (history.isEmpty()) item { Text("No completed games yet. Finish at least one complete round to save a result.") }
        itemsIndexed(history) { _, record ->
            var details by rememberSaveable(record.gameId) { mutableStateOf(false) }
            val result = requireNotNull(record.result)
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${record.configuration.difficulty} • ${DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(record.completedAtEpochMillis!!))}")
                    val winners = result.players.filter { it.playerId in result.winnerPlayerIds }.joinToString { it.displayName }
                    Text("${if (result.tie) "Tie" else "Winner"}: $winners", color = MaterialTheme.colorScheme.primary)
                    Text(result.players.joinToString(" · ") { "${it.displayName} ${it.total}${if (!it.active) " †" else ""}" })
                    Text("${result.completedRoundCount} complete rounds • Casual • Local only", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { details = !details }) { Text(if (details) "Hide details" else "Round details") }
                    if (details) result.rounds.forEachIndexed { r, row ->
                        if (row.any { it != null }) Text("Round ${r + 1}: " + row.mapIndexedNotNull { i, t -> t?.let { "${result.players[i].displayName} ${it.score}" } }.joinToString(" · "))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(ui: GameUiState, model: GameViewModel) {
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Keep screen awake during a game", modifier = Modifier.weight(1f))
            Switch(ui.preferences.keepScreenAwake, model::setKeepScreenAwake, enabled = !ui.busy)
        }
        Text("Games are stored on this device. No account or network connection is needed.")
        if (ui.recoveredRecords > 0) Text("${ui.recoveredRecords} unreadable record(s) retained for recovery.")
        Text(stringResource(R.string.studio_name) + " • Version 0.2.0")
    }
}
