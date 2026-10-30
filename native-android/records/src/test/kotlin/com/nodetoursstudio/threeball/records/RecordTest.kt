package com.nodetoursstudio.threeball.records

import com.nodetoursstudio.threeball.engine.*
import org.junit.Assert.*
import org.junit.Test

class RecordTest {
    private fun start(mode: Difficulty = Difficulty.NOVICE) = Records.start(GameConfig(mode, ScratchPlacement.KITCHEN), listOf("Ana", "Bo"), 1000)
    private fun done(r: GameRecord) = Records.apply(Records.apply(r, Action("HIT", if (r.configuration.difficulty == "MASTER") 3 else 0), 1001), Action("DONE"), 1002)
    private fun rejects(block: () -> Unit) { assertThrows(IllegalArgumentException::class.java, block) }
    @Test fun activeRoundTripRestoresAllCountersAndUndoHistory() {
        var r = start(Difficulty.MASTER)
        r = Records.apply(r, Action("HIT", 2), 1001)
        r = Records.apply(r, Action("SCRATCH"), 1002)
        val restored = RecordJson.decode(RecordJson.encode(r))
        assertEquals(r, restored)
        assertEquals(Records.state(r), Records.state(restored))
        assertEquals(Turn(), Records.state(Records.apply(restored, Action("UNDO"), 1003)).draft)
    }
    @Test fun completedSnapshotHasFinalScoresWinnersAndFullLog() {
        val active = done(done(start(Difficulty.MASTER)))
        val r = Records.apply(active, Action("FINISH"), 1004)
        assertEquals("COMPLETED", r.status)
        assertEquals(listOf(-2, -2), r.result!!.players.map { it.total })
        assertTrue(r.result!!.tie)
        assertEquals(5, r.events.size)
        assertEquals(r, RecordJson.decode(RecordJson.encode(r)))
        assertEquals(GamePhase.FINISHED, Records.state(r).phase)
    }
    @Test fun rejectsTamperedScoreOrEligibility() {
        val complete = Records.apply(done(done(start())), Action("FINISH"), 1005)
        val result = complete.result!!
        rejects { RecordJson.encode(complete.copy(result = result.copy(players = result.players.map { it.copy(total = 0) }))) }
        rejects { RecordJson.encode(complete.copy(result = result.copy(competitive = CompetitiveMetadata(eligible = true)))) }
        rejects { RecordJson.encode(complete.copy(syncEligible = true)) }
    }
    @Test fun rejectsMalformedAndFutureVersionsButIgnoresAdditiveFields() {
        val encoded = RecordJson.encode(start())
        rejects { RecordJson.decode("{") }
        rejects { RecordJson.decode(encoded.replace("\"storageSchemaVersion\":1", "\"storageSchemaVersion\":2")) }
        rejects { RecordJson.decode(encoded.replace("\"storageSchemaVersion\":1,", "")) }
        rejects { RecordJson.decode(encoded.replace("three-ball-2026.1", "future-rules")) }
        assertEquals(start().configuration, RecordJson.decode(encoded.dropLast(1) + ",\"iosFutureField\":true}").configuration)
    }
    @Test fun rejectsInvalidIdentifiersEventOrderAndIllegalActions() {
        val r = Records.apply(start(), Action("HIT", 0), 1001)
        rejects { RecordJson.encode(r.copy(gameId = "not-an-id")) }
        rejects { RecordJson.encode(r.copy(players = listOf(r.players[0], r.players[0]))) }
        rejects { RecordJson.encode(r.copy(events = r.events.map { it.copy(sequence = 4) })) }
        rejects { RecordJson.encode(r.copy(events = r.events.map { it.copy(actorPlayerId = r.players[1].playerId) })) }
        rejects { RecordJson.encode(r.copy(events = r.events.map { it.copy(occurredAtEpochMillis = 0) })) }
        rejects { Records.apply(r, Action("HIT", 0, name = "unexpected"), 1001) }
        rejects { Records.apply(r, Action("CLOUD_UPLOAD"), 1001) }
    }
    @Test fun timestampsSurviveClockRollbackAndRenameKeepsIdentity() {
        val r = start()
        val renamed = Records.apply(r, Action("RENAME", targetPlayerId = r.players[0].playerId, name = "New name"), 0)
        assertEquals(1000L, renamed.events.last().occurredAtEpochMillis)
        assertEquals(r.players[0].playerId, renamed.players[0].playerId)
        assertEquals("New name", Records.state(renamed).players[0].name)
        assertNull(renamed.players[0].accountId)
    }
    @Test fun finalReviewCorrectionSurvivesSerialization() {
        val cfg = GameConfig(Difficulty.MASTER, ScratchPlacement.BALL_IN_HAND, roundLimit = 1)
        val review = done(done(Records.start(cfg, listOf("A", "B"), 1000)))
        val reopened = Records.apply(review, Action("REOPEN", targetPlayerId = review.players[1].playerId, targetRound = 0), 1003)
        val restored = RecordJson.decode(RecordJson.encode(reopened))
        assertNotNull(Records.state(restored).editing)
        val corrected = Records.apply(Records.apply(restored, Action("SCRATCH"), 1004), Action("DONE"), 1005)
        assertEquals(GamePhase.REVIEW, Records.state(corrected).phase)
        assertEquals(1, Records.state(corrected).completedRoundCount)
    }
    @Test fun finishBeforeCompleteRoundIsDiscardedAndDraftNotScored() {
        val r = Records.apply(done(start()), Action("HIT", 0), 1003)
        val finished = Records.apply(r, Action("FINISH"), 1004)
        assertEquals("DISCARDED", finished.status)
        assertNull(finished.result)
        assertEquals(Turn(), Records.state(finished).draft)
    }
    @Test fun completedRecordsCannotAcceptAdditionalEvents() {
        val r = Records.apply(done(done(start())), Action("FINISH"), 1004)
        rejects { Records.apply(r, Action("HIT", 0), 1005) }
        rejects { Records.apply(r, Action("FINISH"), 1005) }
    }
    @Test fun flagsExposeOnlyLocalCapabilities() {
        assertTrue(Features.localScoring && Features.localHistory)
        assertFalse(Features.accounts || Features.cloudSync || Features.regionalLeaderboards || Features.globalLeaderboards)
    }
    @Test fun platformNeutralGoldenFixturesAreCompatible() {
        listOf("active-v1.json", "completed-v1.json", "master-completed-v1.json").forEach { name ->
            val raw = requireNotNull(javaClass.getResourceAsStream("/contract/$name")).bufferedReader().use { it.readText() }
            val decoded = RecordJson.decode(raw)
            assertEquals(decoded, RecordJson.decode(RecordJson.encode(decoded)))
            assertEquals(if (name.startsWith("master")) "MASTER" else "NOVICE", decoded.configuration.difficulty)
        }
    }
    @Test fun standardScratchCorrectionIsExplicitInHistory() {
        val r = Records.apply(Records.apply(start(), Action("SCRATCH"), 1001), Action("UNDO_SCRATCH"), 1002)
        assertEquals(0, Records.state(r).draft.scratches)
        assertEquals(listOf("SCRATCH", "UNDO_SCRATCH"), r.events.map { it.action.type })
    }
}
