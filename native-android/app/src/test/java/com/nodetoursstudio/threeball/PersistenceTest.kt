package com.nodetoursstudio.threeball

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nodetoursstudio.threeball.data.*
import com.nodetoursstudio.threeball.engine.*
import com.nodetoursstudio.threeball.records.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class PersistenceTest {
    private lateinit var context: Context
    private lateinit var db: GameDatabase
    private lateinit var repo: RoomGameRepository
    private val name = "persistence-test.db"
    @Before fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(name)
        open()
    }
    private fun open() {
        db = Room.databaseBuilder(context, GameDatabase::class.java, name)
            .addMigrations(GameDatabase.MIGRATION_1_2).build()
        repo = RoomGameRepository(db, { 2000L })
    }
    @After fun cleanup() { db.close(); context.deleteDatabase(name) }
    private fun record() = Records.start(GameConfig(Difficulty.MASTER, ScratchPlacement.BALL_IN_HAND), listOf("A", "B"), 1000)
    private suspend fun completeRound(): GameRecord {
        var old = record(); repo.save(old, null)
        repeat(2) {
            val hit = Records.apply(old, Action("HIT", 3), 1001); repo.save(hit, old)
            val done = Records.apply(hit, Action("DONE"), 1002); repo.save(done, hit); old = done
        }
        return old
    }
    @Test fun restoresInterruptedHitAfterDatabaseCloseAndReopen() = runBlocking {
        val old = record(); repo.save(old, null)
        val hit = Records.apply(old, Action("HIT", 2), 1001); repo.save(hit, old)
        db.close(); open()
        assertEquals(hit, repo.load().active)
        assertEquals(2, Records.state(repo.load().active!!).draft.pocketed)
    }
    @Test fun finishAndArchiveAreAtomicAndRetryDoesNotDuplicate() = runBlocking {
        val old = completeRound(); val finished = Records.apply(old, Action("FINISH"), 1003)
        repo.finish(finished, old); repo.finish(finished, old)
        val saved = repo.load()
        assertNull(saved.active); assertEquals(listOf(finished), saved.history)
        db.close(); open()
        assertEquals(listOf(finished), repo.load().history)
    }
    @Test fun archiveInsertFailureRollsBackActiveDeletion() = runBlocking {
        val old = completeRound(); val finished = Records.apply(old, Action("FINISH"), 1003)
        db.games().archive(CompletedEntity(old.gameId, "existing immutable row", 1003))
        try { repo.finish(finished, old); fail("Expected unique constraint") } catch (_: android.database.sqlite.SQLiteException) { }
        assertEquals(old, repo.load().active)
        assertEquals("existing immutable row", db.games().completed(old.gameId)!!.payload)
    }
    @Test fun failureAfterArchiveInsertRollsBackBothTables() = runBlocking {
        val old = completeRound(); val finished = Records.apply(old, Action("FINISH"), 1003)
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fail_clear BEFORE DELETE ON active_game BEGIN SELECT RAISE(ABORT, 'simulated failure'); END")
        try { repo.finish(finished, old); fail("Expected delete failure") } catch (_: android.database.sqlite.SQLiteException) { }
        assertEquals(old, repo.load().active)
        assertTrue(repo.load().history.isEmpty())
        db.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_clear")
        repo.finish(finished, old)
        assertEquals(listOf(finished), repo.load().history)
    }
    @Test fun staleUpdatesCannotOverwriteSavedCommands() = runBlocking {
        val old = record(); repo.save(old, null)
        val hit = Records.apply(old, Action("HIT", 1), 1001); repo.save(hit, old)
        try { repo.save(Records.apply(old, Action("HIT", 2), 1001), old); fail("Expected stale update") } catch (_: IllegalStateException) { }
        assertEquals(hit, repo.load().active)
    }
    @Test fun invalidActiveIsQuarantinedWithoutLosingRawBytes() = runBlocking {
        db.games().putActive(ActiveEntity(gameId = "broken", payload = "{broken", revision = 0, updatedAt = 0))
        val loaded = repo.load()
        assertNull(loaded.active); assertEquals(1, loaded.recoveredRecords)
        db.openHelper.readableDatabase.query("SELECT payload FROM quarantined_records").use { cursor ->
            assertTrue(cursor.moveToFirst()); assertEquals("{broken", cursor.getString(0))
        }
        repo.save(record(), null)
        assertNotNull(repo.load().active)
    }
    @Test fun corruptedHistoryIsPreservedAndSkipped() = runBlocking {
        db.games().archive(CompletedEntity("broken", "not JSON", 0))
        val loaded = repo.load()
        assertTrue(loaded.history.isEmpty()); assertEquals(1, loaded.recoveredRecords)
        assertEquals("not JSON", db.games().completed("broken")!!.payload)
    }
    @Test fun noCompletedRoundsMeansNoHistoryEntry() = runBlocking {
        val old = record(); repo.save(old, null)
        repo.finish(Records.apply(old, Action("FINISH"), 1001), old)
        assertNull(repo.load().active); assertTrue(repo.load().history.isEmpty())
    }
    @Test fun migrationOneToTwoPreservesPayloadsAndRoomValidatesSchema() = runBlocking {
        val payload = RecordJson.encode(record())
        db.close(); context.deleteDatabase(name)
        val path = context.getDatabasePath(name); path.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(path, null).use { legacy ->
            val sql = requireNotNull(javaClass.getResourceAsStream("/database-v1.sql")).bufferedReader().use { it.readText() }
            sql.split(';').map { it.trim() }.filter { it.isNotEmpty() }.forEach { legacy.execSQL(it) }
            legacy.execSQL("INSERT INTO active_game (slot, gameId, payload, revision) VALUES (1, ?, ?, 0)", arrayOf(RecordJson.decode(payload).gameId, payload))
            legacy.execSQL("INSERT INTO completed_games (gameId, payload) VALUES ('legacy-invalid', 'preserve me')")
            legacy.version = 1
        }
        open()
        assertEquals(payload, RecordJson.encode(repo.load().active!!))
        assertEquals(0L, db.games().active()!!.updatedAt)
        assertEquals("preserve me", db.games().completed("legacy-invalid")!!.payload)
        assertEquals(2, db.openHelper.readableDatabase.version)
    }
}
