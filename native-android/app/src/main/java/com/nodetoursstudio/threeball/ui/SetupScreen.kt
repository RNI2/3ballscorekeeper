package com.nodetoursstudio.threeball.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.nodetoursstudio.threeball.engine.*
import com.nodetoursstudio.threeball.records.Features

internal fun Difficulty.title() = name.lowercase().replaceFirstChar { it.uppercase() }
internal fun ScratchPlacement.title() = if (this == ScratchPlacement.KITCHEN) "Kitchen" else "Ball in Hand"
private fun Difficulty.subtitle() = when (this) {
    Difficulty.NOVICE -> "Unlimited shots · easygoing play"
    Difficulty.PRO -> "Your choice of shot limit"
    Difficulty.EXPERT -> "Five-shot limit · every hit counts"
    Difficulty.MASTER -> "Pocketed balls, penalties & round limits"
}

@Composable
internal fun DifficultyCard(mode: Difficulty, selected: Boolean, enabled: Boolean, choose: () -> Unit, rules: () -> Unit) {
    SelectionPanel(selected, choose, enabled) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(mode.title(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text((if (selected) "✓ Selected · " else "") + mode.subtitle(), color = TableColors.Muted, fontSize = 13.sp)
            }
            TextButton(onClick = rules, modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "${mode.title()} Rules" }) { Text("Rules") }
        }
    }
}
@Composable
internal fun ScratchRuleCard(rule: ScratchPlacement, selected: Boolean, enabled: Boolean, choose: () -> Unit) {
    SelectionPanel(selected, choose, enabled) {
        Text((if (selected) "✓ " else "") + rule.title(), fontWeight = FontWeight.Bold)
        Text(if (rule == ScratchPlacement.KITCHEN) "Cue ball behind the head string." else "Cue ball anywhere after a scratch.", fontSize = 13.sp, color = TableColors.Muted)
    }
}

@Composable
internal fun SetupScreen(busy: Boolean, start: (GameConfig, List<String>) -> Unit) {
    var modeName by rememberSaveable { mutableStateOf(Difficulty.NOVICE.name) }
    val mode = Difficulty.valueOf(modeName)
    var cap by rememberSaveable { mutableStateOf("6") }
    var rounds by rememberSaveable { mutableStateOf("") }
    var placementName by rememberSaveable { mutableStateOf(ScratchPlacement.BALL_IN_HAND.name) }
    var names by rememberSaveable { mutableStateOf(listOf("", "")) }
    var rules by remember { mutableStateOf<Difficulty?>(null) }
    val config = runCatching { GameConfig(mode, ScratchPlacement.valueOf(placementName),
        if (mode in listOf(Difficulty.PRO, Difficulty.MASTER)) cap.toIntOrNull() else null,
        if (mode == Difficulty.MASTER && rounds.isNotBlank()) requireNotNull(rounds.toIntOrNull()) else null) }.getOrNull()
    val maxPlayers = GameConfig(mode, ScratchPlacement.valueOf(placementName),
        if (mode in listOf(Difficulty.PRO, Difficulty.MASTER)) 6 else null).playerCap
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(TableSpace.Gutter), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RailSection("GAME SETUP") {
            Text("Choose your game", style = MaterialTheme.typography.titleLarge)
            Difficulty.entries.forEach { option ->
                DifficultyCard(option, mode == option, !busy, { modeName = option.name; cap = if (option == Difficulty.MASTER) "6" else "5" }, { rules = option })
            }
            if (mode in listOf(Difficulty.PRO, Difficulty.MASTER)) {
                OutlinedTextField(cap, { cap = it }, label = { Text("Hits per turn (1–99)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, enabled = !busy,
                    isError = (cap.toIntOrNull() ?: 0) !in 1..99, modifier = Modifier.fillMaxWidth())
                if ((cap.toIntOrNull() ?: 0) !in 1..99) Text("Enter a whole number from 1 to 99.", color = MaterialTheme.colorScheme.error)
            }
            if (mode == Difficulty.MASTER) {
                OutlinedTextField(rounds, { rounds = it }, label = { Text("Rounds (1–999, blank = unlimited)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, enabled = !busy,
                    isError = rounds.isNotBlank() && (rounds.toIntOrNull() ?: 0) !in 1..999, modifier = Modifier.fillMaxWidth())
                if (rounds.isNotBlank() && (rounds.toIntOrNull() ?: 0) !in 1..999) Text("Use 1–999 rounds, or leave blank for unlimited play.", color = MaterialTheme.colorScheme.error)
            }
        }
        RailSection("AFTER A SCRATCH") {
            ScratchPlacement.entries.forEach { value -> ScratchRuleCard(value, placementName == value.name, !busy) { placementName = value.name } }
        }
        RailSection("PLAYERS · ${names.size} / $maxPlayers") {
            names.forEachIndexed { index, name ->
                OutlinedTextField(name, { value -> names = names.mapIndexed { i, old -> if (i == index) value else old } },
                    label = { Text("Player ${index + 1}") }, placeholder = { Text("Player ${index + 1}") }, singleLine = true,
                    enabled = !busy, modifier = Modifier.fillMaxWidth())
                if (names.size > 2) TextButton(onClick = { names = names.filterIndexed { i, _ -> i != index } }, enabled = !busy,
                    modifier = Modifier.heightIn(min = 48.dp)) { Text("Remove player ${index + 1}", color = TableColors.Gold) }
            }
            SecondaryActionButton("+ Add player", { names = names + "" }, enabled = !busy && names.size < maxPlayers, modifier = Modifier.fillMaxWidth())
            if (names.size > maxPlayers) Text("This mode allows $maxPlayers players. Remove players or select Master.", color = MaterialTheme.colorScheme.error)
        }
        PrimaryScoreButton("Start Game", { config?.let { start(it, names) } }, enabled = Features.localScoring && !busy && config != null && names.size <= maxPlayers, modifier = Modifier.fillMaxWidth())
        Text("Lowest score wins · saved on this device", color = TableColors.Muted, fontSize = 12.sp)
    }
    rules?.let { selected -> RulesDialog(selected) { rules = null } }
}

@Composable
private fun RulesDialog(mode: Difficulty, dismiss: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss, containerColor = TableColors.Felt, title = { Text("${mode.title()} Rules", style = MaterialTheme.typography.titleLarge) },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(when (mode) {
                Difficulty.NOVICE -> "Unlimited physical shots. Each hit adds 1 point and each scratch adds 1 point."
                Difficulty.PRO -> "Choose a 1–99 hit limit. Each hit adds 1 point and each scratch adds 1 point. Scratches do not consume a hit."
                Difficulty.EXPERT -> "Five physical shots per turn. Each hit and each scratch adds 1 point."
                Difficulty.MASTER -> "Score = shots − balls pocketed + scratches + remaining-ball penalty. Choose a hit limit (default 6). At the cap, each ball left adds 1 point. Clear all three balls or reach the cap before Done. Negative scores are valid."
            })
            Text(if (mode == Difficulty.MASTER) "Tap Hit, select balls pocketed, then add a scratch to that hit if needed. Undo removes the hit, its balls and attached scratch. Optional 1–999 rounds; blank means unlimited."
                else "Tap HIT for each shot, including the break. Undo Last Hit keeps recorded scratches; Undo Scratch corrects a scratch. At the cap, one final scratch is allowed until hit undo resets the allowance. Done saves any positive turn score.")
            Text("Lowest saved total wins; ties stay ties. Back reopens the previous turn. Ball in Hand or Kitchen changes placement, not scoring. Finish explicitly to save history after at least one complete round.")
        } }, confirmButton = { TextButton(onClick = dismiss) { Text("Close rules") } })
}
