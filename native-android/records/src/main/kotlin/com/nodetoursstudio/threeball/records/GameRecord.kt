package com.nodetoursstudio.threeball.records

import com.nodetoursstudio.threeball.engine.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import java.util.UUID

object Features {
    const val localScoring = true
    const val localHistory = true
    const val accounts = false
    const val cloudSync = false
    const val regionalLeaderboards = false
    const val globalLeaderboards = false
}

@Serializable data class Participant(
    val playerId: String,
    val displayName: String,
    val accountId: String? = null,
    val regionId: String? = null,
)
@Serializable data class Configuration(
    val difficulty: String,
    val scratchPlacement: String,
    val shotCap: Int?,
    val roundLimit: Int?,
) {
    fun engine() = GameConfig(Difficulty.valueOf(difficulty), ScratchPlacement.valueOf(scratchPlacement),
        if (difficulty in listOf("PRO", "MASTER")) shotCap else null, roundLimit).also {
        require(it.shotCap == shotCap) { "Inconsistent shot limit" }
    }
    companion object {
        fun from(config: GameConfig) = Configuration(config.difficulty.name, config.placement.name, config.shotCap, config.roundLimit)
    }
}
@Serializable data class Action(
    val type: String,
    val balls: Int? = null,
    val targetPlayerId: String? = null,
    val targetRound: Int? = null,
    val name: String? = null,
    val discardDraft: Boolean = false,
)
@Serializable data class ScoringEvent(
    val eventId: String,
    val sequence: Int,
    val occurredAtEpochMillis: Long,
    val roundIndex: Int,
    val actorPlayerId: String,
    val action: Action,
)
@Serializable data class HitResult(val balls: Int, val scratch: Boolean)
@Serializable data class TurnResult(
    val shots: Int, val scratches: Int, val hits: List<HitResult>, val remainingPenalty: Int, val score: Int,
)
@Serializable data class PlayerResult(
    val playerId: String, val displayName: String, val active: Boolean, val withdrawnRound: Int?, val total: Int,
)
@Serializable data class CompetitiveMetadata(
    val category: String = "CASUAL", val eligible: Boolean = false,
    val reasons: List<String> = listOf("LOCAL_UNVERIFIED"),
)
@Serializable data class FinalResult(
    val players: List<PlayerResult>, val winnerPlayerIds: List<String>, val tie: Boolean,
    val completedRoundCount: Int, val rounds: List<List<TurnResult?>>,
    val competitive: CompetitiveMetadata = CompetitiveMetadata(),
)
@Serializable data class GameRecord(
    val storageSchemaVersion: Int = 1,
    val rulesetVersion: String = "three-ball-2026.1",
    val gameId: String,
    val configuration: Configuration,
    val players: List<Participant>,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long? = null,
    val status: String = "ACTIVE",
    val syncStatus: String = "DISABLED",
    val syncEligible: Boolean = false,
    val events: List<ScoringEvent> = emptyList(),
    val result: FinalResult? = null,
)

/** No Android or storage dependency. The event log is authoritative, including corrections/undo. */
object Records {
    private fun uuid(value: String) = require(UUID.fromString(value).toString() == value) { "Invalid UUID" }
    fun start(config: GameConfig, names: List<String>, now: Long, id: () -> String = { UUID.randomUUID().toString() }): GameRecord {
        require(now >= 0)
        val state = GameEngine.start(config, names)
        return GameRecord(gameId = id(), configuration = Configuration.from(config),
            players = state.players.map { Participant(id(), it.name) }, startedAtEpochMillis = now).also { state(it) }
    }
    private fun initial(record: GameRecord): GameState {
        require(record.storageSchemaVersion == 1) { "Unsupported record version" }
        require(record.rulesetVersion == "three-ball-2026.1") { "Unsupported ruleset" }
        require(record.syncStatus == "DISABLED" && !record.syncEligible) { "Unsupported synchronization policy" }
        uuid(record.gameId)
        require(record.startedAtEpochMillis >= 0)
        require(record.players.map { it.playerId }.distinct().size == record.players.size)
        record.players.forEach { uuid(it.playerId); require(it.displayName.isNotBlank()) }
        return GameEngine.start(record.configuration.engine(), record.players.map { it.displayName })
    }
    private fun transition(record: GameRecord, state: GameState, action: Action, time: Long): GameState {
        val target = record.players.indexOfFirst { it.playerId == action.targetPlayerId }
        // Reject unexpected fields so independently implemented decoders have the same semantics.
        require(action.balls == null || action.type == "HIT")
        require(action.name == null || action.type == "RENAME")
        require(action.targetRound == null || action.type == "REOPEN")
        require(action.targetPlayerId == null || action.type in listOf("RENAME", "REOPEN", "WITHDRAW"))
        require(!action.discardDraft || action.type in listOf("BACK", "REOPEN"))
        return when (action.type) {
            "HIT" -> GameEngine.hit(state, requireNotNull(action.balls))
            "SCRATCH" -> GameEngine.scratch(state)
            "UNDO" -> GameEngine.undoHit(state)
            "UNDO_SCRATCH" -> GameEngine.undoScratch(state)
            "DONE" -> GameEngine.done(state)
            "BACK" -> GameEngine.back(state, action.discardDraft)
            "REOPEN" -> { require(target >= 0); GameEngine.reopenMaster(state, Position(requireNotNull(action.targetRound), target), action.discardDraft) }
            "RENAME" -> { require(target >= 0); GameEngine.rename(state, target, requireNotNull(action.name)) }
            "WITHDRAW" -> { require(target >= 0); GameEngine.withdraw(state, target) }
            "FINISH" -> GameEngine.finish(state, time).state
            else -> throw IllegalArgumentException("Unsupported event type")
        }
    }
    private fun result(record: GameRecord, state: GameState): FinalResult = FinalResult(
        state.players.mapIndexed { index, p -> PlayerResult(record.players[index].playerId, p.name, p.active, p.withdrawnRound, state.totals[index]) },
        state.winnerIndices.map { record.players[it].playerId }, state.winnerIndices.size > 1, state.completedRoundCount,
        state.rounds.map { row -> row.map { t -> t?.let { TurnResult(it.shots, it.scratches,
            it.hits.map { hit -> HitResult(hit.balls, hit.scratch) }, it.remainingPenalty(state.config), it.score(state.config)) } } },
    )
    fun state(record: GameRecord): GameState {
        var state = initial(record)
        var previousTime = record.startedAtEpochMillis
        val ids = mutableSetOf<String>()
        record.events.forEachIndexed { index, event ->
            uuid(event.eventId)
            require(ids.add(event.eventId) && event.sequence == index + 1) { "Invalid event sequence" }
            require(event.occurredAtEpochMillis >= previousTime) { "Invalid event time" }
            require(event.roundIndex == state.position.round && event.actorPlayerId == record.players[state.position.player].playerId) { "Event position mismatch" }
            state = transition(record, state, event.action, event.occurredAtEpochMillis)
            previousTime = event.occurredAtEpochMillis
        }
        if (state.phase == GamePhase.FINISHED) {
            require(record.events.last().action.type == "FINISH")
            require(record.completedAtEpochMillis == previousTime)
            if (state.completedRoundCount > 0) {
                require(record.status == "COMPLETED" && record.result == result(record, state)) { "Result does not match events" }
            } else require(record.status == "DISCARDED" && record.result == null)
        } else require(record.status == "ACTIVE" && record.result == null && record.completedAtEpochMillis == null)
        return state
    }
    fun apply(record: GameRecord, action: Action, now: Long, eventId: String = UUID.randomUUID().toString()): GameRecord {
        val current = state(record)
        val time = maxOf(now, record.events.lastOrNull()?.occurredAtEpochMillis ?: record.startedAtEpochMillis)
        val next = transition(record, current, action, time)
        val event = ScoringEvent(eventId, record.events.size + 1, time, current.position.round,
            record.players[current.position.player].playerId, action)
        val finished = next.phase == GamePhase.FINISHED
        return record.copy(events = record.events + event,
            status = if (!finished) "ACTIVE" else if (next.completedRoundCount > 0) "COMPLETED" else "DISCARDED",
            completedAtEpochMillis = if (finished) time else null,
            result = if (finished && next.completedRoundCount > 0) result(record, next) else null).also { state(it) }
    }
}

object RecordJson {
    private val json = Json { encodeDefaults = true; explicitNulls = true; ignoreUnknownKeys = true }
    fun encode(record: GameRecord): String { Records.state(record); return json.encodeToString(GameRecord.serializer(), record) }
    fun decode(value: String): GameRecord {
        val tree = json.parseToJsonElement(value)
        require(tree.jsonObject.containsKey("storageSchemaVersion") && tree.jsonObject.containsKey("rulesetVersion")) { "Record versions are required" }
        return json.decodeFromJsonElement(GameRecord.serializer(), tree).also { Records.state(it) }
    }
}
