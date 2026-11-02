package com.nodetoursstudio.threeball

import com.nodetoursstudio.threeball.engine.*
import com.nodetoursstudio.threeball.records.*
import com.nodetoursstudio.threeball.ui.*
import org.junit.Assert.*
import org.junit.Test

class PolishPresentationTest {
    private fun start() = Records.start(GameConfig(Difficulty.MASTER, ScratchPlacement.KITCHEN), listOf("A", "B"), 1000)
    private fun ui(record: GameRecord, error: String? = null) = GameUiState(busy = false, loaded = true, active = record, error = error)
    @Test fun feedbackAcknowledgesExactlyOneSuccessfulScoringEvent() {
        val before = start(); val hit = Action("HIT", 3)
        val next = Records.apply(before, hit, 1001)
        assertTrue(acceptedFeedback(before, ui(next), hit))
        assertFalse(acceptedFeedback(before, ui(before), hit))
        assertFalse(acceptedFeedback(before, ui(next, "disk failure"), hit))
        assertFalse(acceptedFeedback(next, ui(next), hit))
        assertFalse(acceptedFeedback(before, ui(next.copy(gameId = start().gameId)), hit))
    }
    @Test fun scratchAndDoneAcknowledgeButUndoAndRestoreDoNot() {
        val hit = Records.apply(start(), Action("HIT", 3), 1001)
        val scratched = Records.apply(hit, Action("SCRATCH"), 1002)
        assertTrue(acceptedFeedback(hit, ui(scratched), Action("SCRATCH")))
        val done = Records.apply(scratched, Action("DONE"), 1003)
        assertTrue(acceptedFeedback(scratched, ui(done), Action("DONE")))
        val undo = Records.apply(hit, Action("UNDO"), 1002)
        assertFalse(acceptedFeedback(hit, ui(undo), Action("UNDO")))
        assertFalse(acceptedFeedback(done, ui(done), Action("DONE")))
    }
    @Test fun unplayedZeroAndNegativeTurnsRemainDistinctInStatistics() {
        var record = start()
        assertNull(standings(Records.state(record))[0].total)
        record = Records.apply(record, Action("HIT", 3), 1001)
        record = Records.apply(record, Action("DONE"), 1002)
        repeat(3) { record = Records.apply(record, Action("HIT", 1), 1003) }
        record = Records.apply(record, Action("DONE"), 1004)
        val rows = standings(Records.state(record))
        assertEquals(-2, rows[0].total); assertEquals(-2, rows[0].best)
        assertEquals(-2.0, rows[0].average!!, 0.0)
        assertEquals(0, rows[1].total); assertEquals(0, rows[1].best)
        assertEquals(0.0, rows[1].average!!, 0.0)
        val complete = Records.apply(record, Action("FINISH"), 1005)
        assertEquals(rows.map { it.copy(current = false) }, standings(complete.result!!))
    }
    @Test fun rankTiesShareMedalsAndWithdrawnPlayersCannotRank() {
        val rows = listOf(Standing("A", -2, listOf(-2), 1, true, true),
            Standing("B", -2, listOf(-2), 0, true, true), Standing("C", 0, listOf(0), 0, true, false),
            Standing("D", -9, listOf(-9), 0, false, false))
        assertEquals(listOf(1, 1, 3, null), rows.map { rank(it, rows) })
        assertEquals(listOf("A", "B", "C", "D"), ranked(rows).map { it.name })
    }
}
