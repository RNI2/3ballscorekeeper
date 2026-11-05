package com.nodetoursstudio.threeball.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.nodetoursstudio.threeball.engine.GameState
import com.nodetoursstudio.threeball.records.*
import java.text.NumberFormat

/** Read-only presentation statistics. Totals and winner status always come from the engine/result. */
internal data class Standing(
    val name: String, val total: Int?, val scores: List<Int>, val scratches: Int,
    val active: Boolean, val winner: Boolean, val current: Boolean = false,
) {
    val best: Int? get() = scores.minOrNull()
    val average: Double? get() = scores.takeIf { it.isNotEmpty() }?.average()
}
internal fun standings(game: GameState): List<Standing> = game.players.mapIndexed { p, player ->
    val turns = game.rounds.mapNotNull { it[p] }
    Standing(player.name, game.totals[p].takeIf { turns.isNotEmpty() }, turns.map { it.score(game.config) },
        turns.sumOf { it.scratches }, player.active, p in game.winnerIndices, p == game.position.player)
}
internal fun standings(result: FinalResult): List<Standing> = result.players.mapIndexed { p, player ->
    val turns = result.rounds.mapNotNull { it[p] }
    Standing(player.displayName, player.total, turns.map { it.score }, turns.sumOf { it.scratches },
        player.active, player.playerId in result.winnerPlayerIds)
}
internal fun ranked(rows: List<Standing>): List<Standing> = rows.sortedWith(compareBy<Standing> { !it.active }
    .thenBy { it.total == null }.thenBy { it.total })
internal fun rank(row: Standing, rows: List<Standing>): Int? = if (!row.active || row.total == null) null else
    1 + rows.count { it.active && it.total != null && it.total < row.total }

@Composable
internal fun CompactStandings(rows: List<Standing>, expanded: Boolean = false) {
    FeltCard {
        Text(if (expanded) "RANKINGS" else "CURRENT STANDINGS", style = MaterialTheme.typography.labelSmall, color = TableColors.Muted)
        if (rows.none { it.total != null }) Text("No saved turns yet", color = TableColors.Muted, fontSize = 12.sp, lineHeight = 16.sp)
        else if (!expanded) ranked(rows).take(3).forEach { row ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RankMedal(rank(row, rows))
                Text(row.name + if (row.winner) " · Leader" else if (!row.active) " · Withdrawn" else "", modifier = Modifier.weight(1f), fontSize = 13.sp, lineHeight = 17.sp)
                Text(row.total?.toString() ?: "—", fontWeight = FontWeight.Bold, color = if (row.winner) TableColors.Gold else TableColors.Cream)
            }
        } else {
            Text("Swipe for BEST, AVG & SCR →", color = TableColors.Muted, fontSize = 12.sp, lineHeight = 16.sp)
            Column(Modifier.horizontalScroll(rememberScrollState())) {
            Row(Modifier.width(550.dp).padding(vertical = 6.dp)) {
                Text("Player", Modifier.width(230.dp), color = TableColors.Muted)
                listOf("PTS", "BEST", "AVG", "SCR").forEach { Text(it, Modifier.width(80.dp), color = TableColors.Muted, fontSize = 12.sp) }
            }
            ranked(rows).forEach { row ->
                Row(Modifier.width(550.dp).clip(RoundedCornerShape(8.dp)).background(if (row.winner) TableColors.Gold.copy(alpha = .13f) else TableColors.CanvasRaised)
                    .border(if (row.winner) .7.dp else .3.dp, if (row.winner) TableColors.Gold.copy(alpha = .65f) else TableColors.Border, RoundedCornerShape(8.dp)).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.width(230.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RankMedal(rank(row, rows))
                        Column(Modifier.weight(1f)) {
                            Text(row.name, fontWeight = FontWeight.Bold)
                            if (!row.active) Text("Withdrawn", fontSize = 11.sp, color = TableColors.Muted)
                            else if (row.winner) Text("Leader", fontSize = 11.sp, color = TableColors.Gold)
                        }
                    }
                    val average = row.average?.let { NumberFormat.getNumberInstance().apply { minimumFractionDigits = 1; maximumFractionDigits = 1 }.format(it) } ?: "—"
                    listOf(row.total?.toString() ?: "—", row.best?.toString() ?: "—", average, row.scratches.toString()).forEach {
                        Text(it, Modifier.width(80.dp), fontWeight = FontWeight.Bold)
                    }
                }
                HorizontalDivider(color = TableColors.Cream.copy(alpha = .08f))
            }
            }
        }
    }
}

@Composable
private fun RankMedal(position: Int?) {
    if (position != null && position <= 3) Box(Modifier.size(24.dp).background(
        when (position) { 1 -> TableColors.Gold; 2 -> Color(0xFFC2D2CC); else -> TableColors.CopperLight }, CircleShape)
        .semantics { contentDescription = "Rank $position" }, contentAlignment = Alignment.Center) {
        Text(position.toString(), color = TableColors.Deep, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Black)
    } else Spacer(Modifier.width(24.dp))
}

@Composable
internal fun RoundSelector(numbers: List<Int>, selected: Int, select: (Int) -> Unit) {
    val scroll = numbers.size > 2 || LocalDensity.current.fontScale > 1.3f
    Row(if (scroll) Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()) else Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        numbers.forEachIndexed { index, number ->
            SecondaryActionButton("Round $number", { select(index) }, modifier = Modifier.semantics { this.selected = selected == index }, highlighted = selected == index)
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SecondaryActionButton("← Previous", { select(selected - 1) }, modifier = Modifier.weight(1f), enabled = selected > 0)
        SecondaryActionButton("Next →", { select(selected + 1) }, modifier = Modifier.weight(1f), enabled = selected < numbers.lastIndex)
    }
}

@Composable
internal fun RoundSummary(number: Int, names: List<String>, turns: List<TurnResult?>,
                          edit: ((Int) -> Unit)? = null, editable: Boolean = false) {
    Text("ROUND $number", style = MaterialTheme.typography.titleMedium)
    names.forEachIndexed { p, name ->
        val turn = turns[p]
        FeltCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(name, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text(turn?.score?.toString() ?: "—", fontWeight = FontWeight.Black, color = TableColors.Gold, fontSize = 24.sp, lineHeight = 28.sp)
            }
            if (turn != null) {
                Text("${turn.shots} shots · ${turn.scratches} scratches", color = TableColors.Muted, fontSize = 12.sp)
                if (turn.remainingPenalty > 0) Text("Remaining-ball penalty +${turn.remainingPenalty}", color = TableColors.Gold, fontSize = 12.sp)
                if (turn.hits.isNotEmpty()) {
                    var hits by remember(number, p) { mutableStateOf(false) }
                    TextButton(onClick = { hits = !hits }, modifier = Modifier.heightIn(min = 48.dp)) { Text(if (hits) "Hide hit details" else "Hit details (${turn.hits.size})") }
                    if (hits) turn.hits.forEachIndexed { h, hit -> Text("Hit ${h + 1}: ${hit.balls} pocketed${if (hit.scratch) " · Scratch +1" else ""}", fontSize = 12.sp) }
                }
                if (edit != null) SecondaryActionButton("Edit $name · round $number", { edit(p) }, enabled = editable && turn.hits.isNotEmpty(), modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
