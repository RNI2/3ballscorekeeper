package com.nodetoursstudio.threeball.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import com.nodetoursstudio.threeball.records.*
import java.text.DateFormat
import java.util.Date

@Composable
internal fun HistoryScreen(history: List<GameRecord>, recovered: Int, initialExpandedId: String? = null) {
    var expandedId by rememberSaveable { mutableStateOf(initialExpandedId) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(TableSpace.Gutter), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RailSection("GAME HISTORY") {
            Text("Every table has a story.", style = MaterialTheme.typography.titleLarge)
            Text("Casual games · saved on this device", color = TableColors.Muted, fontSize = 12.sp)
            if (recovered > 0) Text("$recovered unreadable record(s) preserved for recovery. Valid games remain available.", color = TableColors.Gold)
            if (history.isEmpty()) {
                Text("Your first scorecard awaits", style = MaterialTheme.typography.titleMedium)
                Text("Finish a game with at least one complete round. Your scores and round details will be right here.", color = TableColors.Muted)
            }
        }
        history.forEach { record ->
            val result = requireNotNull(record.result)
            val expanded = expandedId == record.gameId
            RailSection(record.configuration.difficulty + " · " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(record.completedAtEpochMillis!!))) {
                val winners = result.players.filter { it.playerId in result.winnerPlayerIds }.joinToString { it.displayName }
                Text("${if (result.tie) "Tie" else "Winner"}: $winners", color = TableColors.Cream, style = MaterialTheme.typography.titleMedium)
                Text("${result.completedRoundCount} complete rounds · Casual · Local only", color = TableColors.Muted, fontSize = 12.sp)
                if (expanded) CompactStandings(standings(result), true)
                else Text(result.players.take(3).joinToString(" · ") { "${it.displayName} ${it.total}" } + if (result.players.size > 3) " · +${result.players.size - 3} players" else "", color = TableColors.Muted, fontSize = 13.sp)
                SecondaryActionButton(if (expanded) "Hide details" else "Round details", { expandedId = if (expanded) null else record.gameId }, modifier = Modifier.fillMaxWidth())
                if (expanded) {
                    val rounds = result.rounds.indices.filter { r -> result.rounds[r].any { it != null } }
                    var selected by rememberSaveable(record.gameId) { mutableIntStateOf(0) }
                    if (rounds.isNotEmpty()) {
                        val index = selected.coerceIn(0, rounds.lastIndex)
                        RoundSelector(rounds.map { it + 1 }, index) { selected = it }
                        val r = rounds[index]
                        RoundSummary(r + 1, result.players.map { it.displayName }, result.rounds[r])
                    }
                }
            }
        }
    }
}
