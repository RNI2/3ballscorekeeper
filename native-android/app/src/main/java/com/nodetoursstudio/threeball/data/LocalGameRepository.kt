package com.nodetoursstudio.threeball.data

import androidx.room.withTransaction
import com.nodetoursstudio.threeball.records.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Documents are immutable values; callers only publish state after a successful transaction. */
data class SavedGames(val active: GameRecord?, val history: List<GameRecord>, val recoveredRecords: Int = 0)
interface LocalGameRepository {
    suspend fun load(): SavedGames
    suspend fun save(next: GameRecord, previous: GameRecord?)
    suspend fun finish(next: GameRecord, previous: GameRecord)
    suspend fun abandon(previous: GameRecord)
}

class RoomGameRepository(private val db: GameDatabase, private val clock: () -> Long = System::currentTimeMillis) : LocalGameRepository {
    private val dao get() = db.games()
    private fun decode(payload: String): GameRecord? = try { RecordJson.decode(payload) }
        catch (e: CancellationException) { throw e }
        catch (_: IllegalArgumentException) { null }
    override suspend fun load(): SavedGames = db.withTransaction {
        var invalid = 0
        val row = dao.active()
        val document = row?.let { decode(it.payload) }
        val active = document?.takeIf { it.status == "ACTIVE" && it.gameId == row.gameId && it.events.size == row.revision }
        if (row != null && active == null) {
            // Preserve the original bytes for recovery rather than deleting the only copy.
            dao.quarantine(QuarantinedEntity(row.gameId, row.payload, clock()))
            dao.clearActive()
        }
        val history = dao.history().mapNotNull { saved ->
            decode(saved.payload)?.takeIf { it.status == "COMPLETED" && it.gameId == saved.gameId }
                .also { if (it == null) invalid++ }
        }
        SavedGames(active, history, dao.quarantineCount() + invalid)
    }
    private suspend fun expect(previous: GameRecord?) {
        val current = dao.active()
        if (previous == null) check(current == null) { "An active game already exists" }
        else check(current != null && current.gameId == previous.gameId && current.revision == previous.events.size &&
            decode(current.payload) == previous) { "Saved game changed; reload before continuing" }
    }
    private fun continuation(next: GameRecord, previous: GameRecord) {
        check(next.events.size == previous.events.size + 1)
        check(next.copy(events = next.events.dropLast(1), status = "ACTIVE", completedAtEpochMillis = null, result = null) == previous)
    }
    override suspend fun save(next: GameRecord, previous: GameRecord?) {
        val payload = withContext(Dispatchers.Default) { RecordJson.encode(next) }
        require(next.status == "ACTIVE")
        if (previous != null) continuation(next, previous) else require(next.events.isEmpty())
        db.withTransaction {
            expect(previous)
            dao.putActive(ActiveEntity(gameId = next.gameId, payload = payload, revision = next.events.size, updatedAt = clock()))
        }
    }
    override suspend fun finish(next: GameRecord, previous: GameRecord) {
        val payload = withContext(Dispatchers.Default) { RecordJson.encode(next) }
        require(next.status in listOf("COMPLETED", "DISCARDED"))
        continuation(next, previous)
        db.withTransaction {
            // Retrying an already committed completion must not duplicate or alter the immutable result.
            val existing = dao.completed(next.gameId)
            if (dao.active() == null && existing?.payload == payload) return@withTransaction
            expect(previous)
            if (next.status == "COMPLETED") dao.archive(CompletedEntity(next.gameId, payload, requireNotNull(next.completedAtEpochMillis)))
            dao.clearActive()
        }
    }
    override suspend fun abandon(previous: GameRecord) = db.withTransaction { expect(previous); dao.clearActive() }
}
