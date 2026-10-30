package com.nodetoursstudio.threeball.engine

/** Pure transitions. Invalid actions fail before changing the caller's state. */
object GameEngine {
    fun start(config: GameConfig, names: List<String>): GameState {
        require(names.size in 2..config.playerCap) { "Invalid player count" }
        return GameState(config, names.mapIndexed { i, name -> Player(name.trim().ifEmpty { "Player ${i + 1}" }) },
            listOf(List(names.size) { null }))
    }

    private fun playable(game: GameState) = require(game.phase == GamePhase.PLAYING) { "Game is not accepting turns" }

    fun hit(game: GameState, balls: Int = 0): GameState {
        playable(game)
        val config = game.config
        val turn = game.draft
        require(config.shotCap == null || turn.shots < config.shotCap!!) { "Shot limit reached" }
        return if (config.difficulty == Difficulty.MASTER) {
            require(turn.pocketed < 3 && balls in 0..(3 - turn.pocketed)) { "Invalid pocketed balls" }
            game.copy(draft = turn.copy(shots = turn.shots + 1, hits = turn.hits + Hit(balls)))
        } else {
            require(balls == 0 && turn.score(config) < 999)
            game.copy(draft = turn.copy(shots = turn.shots + 1))
        }
    }

    fun scratch(game: GameState): GameState {
        playable(game)
        val turn = game.draft
        if (game.config.difficulty == Difficulty.MASTER) {
            val last = turn.hits.lastOrNull()
            require(last != null && !last.scratch) { "Scratch requires an unscratched latest hit" }
            return game.copy(draft = turn.copy(scratches = turn.scratches + 1,
                hits = turn.hits.dropLast(1) + last.copy(scratch = true)))
        }
        val atCap = game.config.shotCap?.let { turn.shots >= it } ?: false
        require(turn.score(game.config) < 999 && !(atCap && turn.finalScratchUsed))
        return game.copy(draft = turn.copy(scratches = turn.scratches + 1,
            finalScratchUsed = turn.finalScratchUsed || atCap))
    }

    fun undoHit(game: GameState): GameState {
        playable(game)
        val turn = game.draft
        require(turn.shots > 0) { "No hit to undo" }
        return if (game.config.difficulty == Difficulty.MASTER) {
            val last = turn.hits.last()
            game.copy(draft = turn.copy(shots = turn.shots - 1, hits = turn.hits.dropLast(1),
                scratches = turn.scratches - if (last.scratch) 1 else 0))
        } else game.copy(draft = turn.copy(shots = turn.shots - 1, finalScratchUsed = false))
    }

    /** Native correction for an accidental standard-mode scratch. Master uses per-hit undo. */
    fun undoScratch(game: GameState): GameState {
        playable(game)
        require(game.config.difficulty != Difficulty.MASTER && game.draft.scratches > 0)
        return game.copy(draft = game.draft.copy(scratches = game.draft.scratches - 1, finalScratchUsed = false))
    }

    private fun setTurn(game: GameState, position: Position, turn: Turn?): List<List<Turn?>> =
        game.rounds.mapIndexed { r, row -> if (r != position.round) row else
            row.mapIndexed { p, old -> if (p == position.player) turn else old } }

    fun done(game: GameState): GameState {
        playable(game)
        require(game.draft.canComplete(game.config)) { "Turn is incomplete" }
        val saved = game.copy(rounds = setTurn(game, game.position, game.draft), draft = Turn())
        game.editing?.let { return saved.copy(position = it.position, phase = it.phase, editing = null) }
        val active = game.activeIndices
        val index = active.indexOf(game.position.player)
        if (index < active.lastIndex) return saved.copy(position = Position(game.position.round, active[index + 1]))
        if (game.config.roundLimit?.let { game.position.round + 1 >= it } == true) {
            return saved.copy(position = Position(game.position.round, active.first()), phase = GamePhase.REVIEW)
        }
        val nextRound = game.position.round + 1
        return saved.copy(position = Position(nextRound, active.first()), rounds =
            if (nextRound < saved.rounds.size) saved.rounds else saved.rounds + listOf(List(game.players.size) { null }))
    }

    /** UI must ask before setting discardDraft=true if an unfinished draft exists. */
    fun back(game: GameState, discardDraft: Boolean = false): GameState {
        require(game.phase != GamePhase.FINISHED && game.editing == null)
        require(!game.draft.hasEntries || discardDraft) { "Confirm discarding the draft" }
        val active = game.activeIndices
        val current = if (game.phase == GamePhase.REVIEW) active.first() else game.position.player
        val index = active.indexOf(current)
        val target = when {
            index > 0 -> Position(game.position.round, active[index - 1])
            game.position.round > 0 -> Position(game.position.round - 1, active.last())
            else -> Position(0, active.first())
        }
        val turn = game.rounds[target.round][target.player] ?: Turn()
        return game.copy(position = target, draft = turn.copy(finalScratchUsed = false),
            rounds = setTurn(game, target, null), phase = GamePhase.PLAYING)
    }

    /** Master scorecard correction restores the original destination after Done. */
    fun reopenMaster(game: GameState, target: Position, discardDraft: Boolean = false): GameState {
        require(game.phase != GamePhase.FINISHED && game.config.difficulty == Difficulty.MASTER && game.editing == null)
        require(!game.draft.hasEntries || discardDraft) { "Confirm discarding the draft" }
        val record = game.rounds.getOrNull(target.round)?.getOrNull(target.player)
        require(record != null) { "No saved turn at this position" }
        return game.copy(position = target, draft = record, rounds = setTurn(game, target, null),
            phase = GamePhase.PLAYING, editing = EditDestination(game.position, game.phase))
    }

    fun rename(game: GameState, player: Int, name: String): GameState {
        require(game.phase != GamePhase.FINISHED && player in game.players.indices)
        return game.copy(players = game.players.mapIndexed { i, p -> if (i == player)
            p.copy(name = name.trim().ifEmpty { "Player ${i + 1}" }) else p })
    }

    /** Matches the visible web Withdraw control: current player resets to first active. */
    fun withdraw(game: GameState, player: Int): GameState {
        require(game.phase != GamePhase.FINISHED)
        require(game.editing == null) { "Finish correcting the turn first" }
        require(player in game.activeIndices && game.activeIndices.size > 2)
        val updated = game.copy(players = game.players.mapIndexed { i, p -> if (i == player)
            p.copy(active = false, withdrawnRound = game.position.round + 1) else p })
        return if (game.position.player == player) updated.copy(draft = Turn(),
            position = Position(game.position.round, updated.activeIndices.first())) else updated
    }

    /** Includes saved partial rounds; the unfinished draft is never archived. */
    fun finish(game: GameState, finishedAtEpochMillis: Long): FinishResult {
        require(game.phase != GamePhase.FINISHED && game.editing == null) { "Finish correcting the turn first" }
        val finished = game.copy(phase = GamePhase.FINISHED, draft = Turn())
        return FinishResult(finished, if (finished.completedRoundCount > 0) CompletedGame(finishedAtEpochMillis, finished) else null)
    }
}
