package com.nodetoursstudio.threeball

import androidx.lifecycle.ViewModelStore
import com.nodetoursstudio.threeball.data.*
import com.nodetoursstudio.threeball.engine.*
import com.nodetoursstudio.threeball.records.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {
    private val store = ViewModelStore()
    private val repo = FakeGames()
    private val prefs = FakePreferences()
    @Before fun setup() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun cleanup() { store.clear(); Dispatchers.resetMain() }
    private fun model(): GameViewModel = GameViewModel(repo, prefs, { 1000L }, Dispatchers.Main).also { store.put("model", it) }
    private val config = GameConfig(Difficulty.NOVICE, ScratchPlacement.BALL_IN_HAND)

    @Test fun startsAndRestoresPersistedGame() = runTest {
        val m = model(); advanceUntilIdle()
        m.start(config, listOf("A", "B")); advanceUntilIdle()
        m.command(Action("HIT", 0)); advanceUntilIdle()
        val saved = m.ui.value.active
        val restored = model(); advanceUntilIdle()
        assertEquals(saved, restored.ui.value.active)
        assertEquals(1, restored.ui.value.game!!.draft.shots)
        assertFalse(restored.ui.value.busy)
    }
    @Test fun doesNotPublishUncommittedScoringAndRecoversFromSaveFailure() = runTest {
        val m = model(); advanceUntilIdle(); m.start(config, listOf("A", "B")); advanceUntilIdle()
        repo.gate = CompletableDeferred()
        m.command(Action("HIT", 0)); runCurrent()
        assertTrue(m.ui.value.busy)
        assertEquals(0, m.ui.value.game!!.draft.shots)
        repo.fail = true; repo.gate!!.complete(Unit); advanceUntilIdle()
        assertEquals(0, m.ui.value.game!!.draft.shots)
        assertNotNull(m.ui.value.error)
        repo.fail = false; repo.gate = null
        m.command(Action("HIT", 0)); advanceUntilIdle()
        assertEquals(1, m.ui.value.game!!.draft.shots)
    }
    @Test fun serializesRapidCommandsWithoutLostUpdates() = runTest {
        val m = model(); advanceUntilIdle(); m.start(config, listOf("A", "B")); advanceUntilIdle()
        repeat(3) { m.command(Action("HIT", 0)) }
        advanceUntilIdle()
        assertEquals(3, m.ui.value.game!!.draft.shots)
        assertEquals(3, repo.active!!.events.size)
    }
    @Test fun completionPublishesHistoryOnlyAfterArchiveSucceeds() = runTest {
        val m = model(); advanceUntilIdle(); m.start(config, listOf("A", "B")); advanceUntilIdle()
        repeat(2) { m.command(Action("HIT", 0)); m.command(Action("DONE")) }; advanceUntilIdle()
        repo.fail = true; m.command(Action("FINISH")); advanceUntilIdle()
        assertNotNull(m.ui.value.active); assertTrue(m.ui.value.history.isEmpty())
        repo.fail = false; m.command(Action("FINISH")); advanceUntilIdle()
        assertNull(m.ui.value.active); assertEquals(1, m.ui.value.history.size)
    }
    @Test fun invalidCommandLeavesPersistedStateUnchanged() = runTest {
        val m = model(); advanceUntilIdle(); m.start(config, listOf("A", "B")); advanceUntilIdle()
        val saved = repo.active
        m.command(Action("DONE")); advanceUntilIdle()
        assertEquals(saved, repo.active); assertNotNull(m.ui.value.error)
    }
    @Test fun loadFailureCanRetryWithoutDestructiveReset() = runTest {
        repo.fail = true
        val m = model(); advanceUntilIdle()
        assertFalse(m.ui.value.loaded); assertNotNull(m.ui.value.error)
        repo.fail = false; m.reload(); advanceUntilIdle()
        assertTrue(m.ui.value.loaded); assertNull(m.ui.value.error)
    }
    @Test fun preferenceChangesAndAbandonAreLocal() = runTest {
        val m = model(); advanceUntilIdle()
        m.setKeepScreenAwake(false); advanceUntilIdle()
        assertFalse(m.ui.value.preferences.keepScreenAwake)
        m.start(config, listOf("A", "B")); advanceUntilIdle()
        m.abandon(); advanceUntilIdle()
        assertNull(repo.active); assertTrue(repo.history.isEmpty()); assertNull(m.ui.value.active)
    }
    @Test fun restoresMasterFinalReviewAndQuarantineNotice() = runTest {
        var r = Records.start(GameConfig(Difficulty.MASTER, ScratchPlacement.KITCHEN, roundLimit = 1), listOf("A", "B"), 1000)
        repeat(2) { r = Records.apply(Records.apply(r, Action("HIT", 3), 1001), Action("DONE"), 1002) }
        repo.active = r; repo.recovered = 1
        val m = model(); advanceUntilIdle()
        assertEquals(GamePhase.REVIEW, m.ui.value.game!!.phase)
        assertEquals(1, m.ui.value.recoveredRecords)
    }

    private class FakePreferences : PreferencesRepository {
        override val preferences = MutableStateFlow(GamePreferences())
        override suspend fun setKeepScreenAwake(value: Boolean) { preferences.value = GamePreferences(value) }
    }
    private class FakeGames : LocalGameRepository {
        var active: GameRecord? = null
        val history = mutableListOf<GameRecord>()
        var recovered = 0
        var fail = false
        var gate: CompletableDeferred<Unit>? = null
        private suspend fun checkFailure() { gate?.await(); if (fail) throw IOException("Test disk failure") }
        override suspend fun load(): SavedGames { checkFailure(); return SavedGames(active, history.toList(), recovered) }
        override suspend fun save(next: GameRecord, previous: GameRecord?) { checkFailure(); check(active == previous); active = next }
        override suspend fun finish(next: GameRecord, previous: GameRecord) {
            checkFailure(); check(active == previous)
            if (next.result != null) history.add(next)
            active = null
        }
        override suspend fun abandon(previous: GameRecord) { checkFailure(); check(active == previous); active = null }
    }
}
