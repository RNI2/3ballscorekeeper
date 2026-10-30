package com.nodetoursstudio.threeball.engine

enum class Difficulty { NOVICE, PRO, EXPERT, MASTER }
enum class ScratchPlacement { BALL_IN_HAND, KITCHEN }
enum class GamePhase { PLAYING, REVIEW, FINISHED }

data class GameConfig(
    val difficulty: Difficulty,
    val placement: ScratchPlacement,
    val customShotCap: Int? = if (difficulty == Difficulty.MASTER) 6 else null,
    val roundLimit: Int? = null,
) {
    init {
        if (difficulty == Difficulty.PRO || difficulty == Difficulty.MASTER) {
            require(customShotCap != null && customShotCap in 1..99) { "Shot cap must be 1..99" }
        } else require(customShotCap == null) { "This mode uses its own shot limit" }
        require(roundLimit == null || (difficulty == Difficulty.MASTER && roundLimit in 1..999))
    }
    val shotCap: Int? get() = when (difficulty) {
        Difficulty.NOVICE -> null
        Difficulty.EXPERT -> 5
        else -> customShotCap
    }
    val playerCap: Int get() = if (difficulty == Difficulty.MASTER) 20 else 6
}

data class Player(val name: String, val active: Boolean = true, val withdrawnRound: Int? = null)
data class Hit(val balls: Int, val scratch: Boolean = false)

/** Standard scratches are separate counters; Master scratches belong to a hit. */
data class Turn(
    val shots: Int = 0,
    val scratches: Int = 0,
    val hits: List<Hit> = emptyList(),
    val finalScratchUsed: Boolean = false,
) {
    val pocketed: Int get() = hits.sumOf { it.balls }
    fun remainingPenalty(config: GameConfig): Int =
        if (config.difficulty == Difficulty.MASTER && shots >= config.shotCap!! && pocketed < 3) 3 - pocketed else 0
    fun score(config: GameConfig): Int = shots + scratches -
        (if (config.difficulty == Difficulty.MASTER) pocketed else 0) + remainingPenalty(config)
    fun canComplete(config: GameConfig): Boolean = if (config.difficulty == Difficulty.MASTER)
        pocketed == 3 || shots >= config.shotCap!! else score(config) >= 1
    val hasEntries: Boolean get() = shots != 0 || scratches != 0
}

data class Position(val round: Int, val player: Int)
data class EditDestination(val position: Position, val phase: GamePhase)
data class GameState(
    val config: GameConfig,
    val players: List<Player>,
    val rounds: List<List<Turn?>>,
    val position: Position = Position(0, 0),
    val draft: Turn = Turn(),
    val phase: GamePhase = GamePhase.PLAYING,
    val editing: EditDestination? = null,
) {
    val activeIndices: List<Int> get() = players.indices.filter { players[it].active }
    val totals: List<Int> get() = players.indices.map { p -> rounds.sumOf { it[p]?.score(config) ?: 0 } }
    fun isRoundComplete(index: Int): Boolean = players.indices.all { p ->
        val player = players[p]
        (!player.active && player.withdrawnRound == null) ||
            (player.withdrawnRound != null && index + 1 >= player.withdrawnRound) || rounds[index][p] != null
    }
    val completedRoundCount: Int get() = rounds.indices.count { isRoundComplete(it) }
    val winnerIndices: List<Int> get() {
        val eligible = activeIndices.filter { p -> rounds.any { it[p] != null } }
        val low = eligible.minOfOrNull { totals[it] }
        return eligible.filter { totals[it] == low }
    }
}

/** Timestamp supplied by the caller; no clock, Android, or storage dependencies. */
data class CompletedGame(val finishedAtEpochMillis: Long, val game: GameState)
data class FinishResult(val state: GameState, val archive: CompletedGame?)

/** Future local store must atomically persist the active snapshot and history on finish. */
interface GameRepository {
    suspend fun loadActive(): GameState?
    suspend fun loadHistory(): List<CompletedGame>
    suspend fun saveActive(game: GameState)
    suspend fun finish(result: FinishResult)
    suspend fun abandon()
}
