package com.nodetoursstudio.threeball.engine

import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {
    private fun game(mode: Difficulty = Difficulty.NOVICE, cap: Int? = null, rounds: Int? = null,
                     players: Int = 2, placement: ScratchPlacement = ScratchPlacement.BALL_IN_HAND): GameState =
        GameEngine.start(GameConfig(mode, placement, cap ?: if (mode == Difficulty.MASTER) 6 else null, rounds),
            List(players) { "Player ${it + 1}" })
    private fun complete(g: GameState): GameState = GameEngine.done(GameEngine.hit(g,
        if (g.config.difficulty == Difficulty.MASTER) 3 else 0))
    private fun rejects(action: () -> Unit) {
        assertThrows(IllegalArgumentException::class.java, action)
    }

    @Test fun scoringVectorsMatchWebReference() {
        val lines = requireNotNull(javaClass.getResourceAsStream("/scoring-vectors.tsv"))
            .bufferedReader().use { it.readLines() }
        lines.filter { !it.startsWith("#") && it.isNotBlank() }.forEach { line ->
            val v = line.split(' ')
            var g = game(Difficulty.valueOf(v[1]), v[2].toIntOrNull())
            if (v[3] != "-") v[3].split(',').forEach { action ->
                g = when (action.first()) {
                    'H' -> GameEngine.hit(g, action.drop(1).toInt())
                    'S' -> GameEngine.scratch(g)
                    'U' -> GameEngine.undoHit(g)
                    else -> error("Unknown action")
                }
            }
            assertEquals(v[0], v[4].toInt(), g.draft.score(g.config))
            assertEquals(v[0], v[5].toInt(), g.draft.shots)
            assertEquals(v[0], v[6].toInt(), g.draft.pocketed)
            assertEquals(v[0], v[7].toInt(), g.draft.scratches)
            assertEquals(v[0], v[8].toBoolean(), g.draft.canComplete(g.config))
        }
    }

    @Test fun configValidatesModeSpecificLimits() {
        rejects { game(Difficulty.PRO) }
        rejects { game(Difficulty.PRO, 0) }
        rejects { game(Difficulty.MASTER, 100) }
        rejects { game(rounds = 1) }
        rejects { game(Difficulty.MASTER, rounds = 1000) }
        rejects { game(Difficulty.EXPERT, 6) }
        assertEquals(6, game(Difficulty.MASTER).config.shotCap)
        assertEquals(5, game(Difficulty.EXPERT).config.shotCap)
        assertNull(game().config.shotCap)
    }
    @Test fun playerCapsAreModeSpecific() {
        rejects { game(players = 1) }
        rejects { game(players = 7) }
        assertEquals(20, game(Difficulty.MASTER, players = 20).players.size)
        rejects { game(Difficulty.MASTER, players = 21) }
    }
    @Test fun blankNamesUseDefaultsAndRenameWorksDuringPlay() {
        val g = GameEngine.start(GameConfig(Difficulty.NOVICE, ScratchPlacement.KITCHEN), listOf(" ", " Jane "))
        assertEquals(listOf("Player 1", "Jane"), g.players.map { it.name })
        assertEquals("Alex", GameEngine.rename(g, 0, " Alex ").players[0].name)
    }
    @Test fun scratchPlacementDoesNotChangeScoring() {
        val a = GameEngine.scratch(GameEngine.hit(game()))
        val b = GameEngine.scratch(GameEngine.hit(game(placement = ScratchPlacement.KITCHEN)))
        assertEquals(a.draft.score(a.config), b.draft.score(b.config))
    }
    @Test fun noviceHasNoShotCapButScoreInputStopsAt999() {
        var g = game()
        repeat(999) { g = GameEngine.hit(g) }
        assertEquals(999, g.draft.score(g.config))
        rejects { GameEngine.hit(g) }
        rejects { GameEngine.scratch(g) }
    }
    @Test fun standardScratchCanPrecedeHitAndDoesNotConsumeShotLimit() {
        var g = GameEngine.scratch(game(Difficulty.PRO, 1))
        assertTrue(g.draft.canComplete(g.config))
        g = GameEngine.hit(g)
        assertEquals(1, g.draft.shots)
        assertEquals(2, g.draft.score(g.config))
        rejects { GameEngine.hit(g) }
    }
    @Test fun onlyOneStandardScratchAfterCapUntilUndo() {
        val g = GameEngine.scratch(GameEngine.hit(game(Difficulty.PRO, 1)))
        rejects { GameEngine.scratch(g) }
        val again = GameEngine.scratch(GameEngine.hit(GameEngine.undoHit(g)))
        assertEquals(2, again.draft.scratches)
    }
    @Test fun standardUndoKeepsScratchCounter() {
        val g = GameEngine.undoHit(GameEngine.scratch(GameEngine.hit(game())))
        assertEquals(1, g.draft.scratches)
        assertEquals(0, g.draft.shots)
        rejects { GameEngine.undoHit(g) }
    }
    @Test fun masterScratchRequiresHitAndCannotRepeatOnSameHit() {
        val g = game(Difficulty.MASTER)
        rejects { GameEngine.scratch(g) }
        val scratched = GameEngine.scratch(GameEngine.hit(g, 3))
        rejects { GameEngine.scratch(scratched) }
        rejects { GameEngine.hit(scratched, 0) }
        assertEquals(-1, scratched.draft.score(g.config))
    }
    @Test fun masterRejectsInvalidBallCountsAndCaps() {
        val g = GameEngine.hit(game(Difficulty.MASTER, 2), 2)
        rejects { GameEngine.hit(g, -1) }
        rejects { GameEngine.hit(g, 2) }
        val capped = GameEngine.hit(g, 0)
        rejects { GameEngine.hit(capped, 0) }
        assertEquals(1, capped.draft.remainingPenalty(g.config))
    }
    @Test fun doneRequiresAnEligibleTurn() {
        rejects { GameEngine.done(game()) }
        rejects { GameEngine.done(GameEngine.hit(game(Difficulty.MASTER), 1)) }
    }
    @Test fun doneAdvancesPlayerThenRoundAndResetsDraft() {
        val first = complete(game())
        assertEquals(Position(0, 1), first.position)
        assertEquals(Turn(), first.draft)
        val second = complete(first)
        assertEquals(Position(1, 0), second.position)
        assertEquals(1, second.completedRoundCount)
        assertEquals(listOf(1, 1), second.totals)
        assertEquals(listOf(0, 1), second.winnerIndices)
    }
    @Test fun masterFinalRoundWaitsForReviewAndExplicitFinish() {
        val review = complete(complete(game(Difficulty.MASTER, rounds = 1)))
        assertEquals(GamePhase.REVIEW, review.phase)
        assertEquals(1, review.rounds.size)
        rejects { GameEngine.hit(review) }
        rejects { GameEngine.done(review) }
        val result = GameEngine.finish(review, 123L)
        assertEquals(123L, result.archive!!.finishedAtEpochMillis)
        assertEquals(GamePhase.FINISHED, result.state.phase)
        rejects { GameEngine.finish(result.state, 124L) }
    }
    @Test fun masterUnlimitedContinuesAndNegativeScoresWin() {
        val g = complete(complete(game(Difficulty.MASTER)))
        assertEquals(GamePhase.PLAYING, g.phase)
        assertEquals(Position(1, 0), g.position)
        assertEquals(listOf(-2, -2), g.totals)
    }
    @Test fun backRequiresDiscardThenRestoresPreviousTurn() {
        val g = GameEngine.hit(complete(game()))
        rejects { GameEngine.back(g) }
        val back = GameEngine.back(g, discardDraft = true)
        assertEquals(Position(0, 0), back.position)
        assertEquals(1, back.draft.shots)
        assertNull(back.rounds[0][0])
        assertEquals(Position(0, 0), GameEngine.back(game()).position)
    }
    @Test fun backAcrossRoundBoundaryRestoresMasterHitAndScratch() {
        val first = GameEngine.done(GameEngine.scratch(GameEngine.hit(game(Difficulty.MASTER), 3)))
        val next = complete(first)
        val back = GameEngine.back(next)
        assertEquals(Position(0, 1), back.position)
        assertEquals(listOf(Hit(3)), back.draft.hits)
        val previous = GameEngine.back(back, discardDraft = true)
        assertEquals(listOf(Hit(3, true)), previous.draft.hits)
        assertEquals(1, previous.draft.scratches)
    }
    @Test fun masterCorrectionReturnsToOriginalDestinationAndReviewPhase() {
        val review = complete(complete(game(Difficulty.MASTER, rounds = 1)))
        val editing = GameEngine.reopenMaster(review, Position(0, 1))
        assertEquals(0, editing.completedRoundCount)
        rejects { GameEngine.back(editing) }
        rejects { GameEngine.finish(editing, 1L) }
        val corrected = GameEngine.done(GameEngine.scratch(editing))
        assertEquals(review.position, corrected.position)
        assertEquals(GamePhase.REVIEW, corrected.phase)
        assertEquals(1, corrected.completedRoundCount)
        assertEquals(listOf(0), corrected.winnerIndices)
    }
    @Test fun masterCorrectionProtectsUnfinishedDraft() {
        val g = GameEngine.hit(complete(game(Difficulty.MASTER)), 1)
        rejects { GameEngine.reopenMaster(g, Position(0, 0)) }
        val editing = GameEngine.reopenMaster(g, Position(0, 0), discardDraft = true)
        assertEquals(g.position, GameEngine.done(editing).position)
    }
    @Test fun withdrawalPreservesScoresExcludesWinnerAndSkipsTurns() {
        var g = complete(game(players = 3))
        g = GameEngine.withdraw(g, 0)
        g = complete(complete(g))
        assertEquals(Position(1, 1), g.position)
        assertEquals(listOf(1, 1, 1), g.totals)
        assertEquals(listOf(1, 2), g.winnerIndices)
        assertEquals(1, g.completedRoundCount)
        rejects { GameEngine.withdraw(g, 1) }
    }
    @Test fun withdrawingCurrentPlayerClearsDraftAndSelectsFirstActive() {
        val g = GameEngine.withdraw(GameEngine.hit(game(players = 3)), 0)
        assertEquals(Position(0, 1), g.position)
        assertEquals(Turn(), g.draft)
        assertEquals(1, g.players[0].withdrawnRound)
    }
    @Test fun withdrawalDoesNotMakeEarlierIncompleteRoundsComplete() {
        var g = complete(game(players = 3))
        g = complete(complete(g))
        // Master reopening is tested separately; an earlier blank score must still count.
        val rows = g.rounds.mapIndexed { i, row -> if (i == 0) row.mapIndexed { p, t -> if (p == 2) null else t } else row }
        g = GameEngine.withdraw(g.copy(rounds = rows), 2)
        assertEquals(0, g.completedRoundCount)
    }
    @Test fun finishWithoutCompleteRoundProducesNoHistory() {
        val result = GameEngine.finish(complete(game()), 0L)
        assertNull(result.archive)
        assertEquals(GamePhase.FINISHED, result.state.phase)
    }
    @Test fun archiveIncludesSavedPartialRoundButDiscardsDraft() {
        var g = complete(complete(game()))
        g = complete(g)
        g = GameEngine.hit(g)
        val archive = GameEngine.finish(g, 100L).archive!!.game
        assertEquals(listOf(2, 1), archive.totals)
        assertEquals(Turn(), archive.draft)
        assertEquals(1, archive.completedRoundCount)
        assertNull(archive.rounds[1][1])
    }
    @Test fun transitionsDoNotMutatePriorSnapshot() {
        val g = game(Difficulty.MASTER)
        val next = GameEngine.scratch(GameEngine.hit(g, 3))
        GameEngine.done(next)
        assertEquals(Turn(), g.draft)
        assertNull(g.rounds[0][0])
        assertEquals(listOf(Hit(3, true)), next.draft.hits)
    }
    @Test fun withdrawalDuringFinalReviewKeepsReviewAndExcludesPlayer() {
        val review = complete(complete(complete(game(Difficulty.MASTER, rounds = 1, players = 3))))
        val withdrawn = GameEngine.withdraw(review, 0)
        assertEquals(GamePhase.REVIEW, withdrawn.phase)
        assertEquals(Position(0, 1), withdrawn.position)
        assertEquals(listOf(1, 2), withdrawn.winnerIndices)
        assertEquals(1, withdrawn.completedRoundCount)
    }

}
