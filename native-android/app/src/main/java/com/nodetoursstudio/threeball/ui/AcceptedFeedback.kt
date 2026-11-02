package com.nodetoursstudio.threeball.ui

import com.nodetoursstudio.threeball.GameUiState
import com.nodetoursstudio.threeball.records.*

/** Feedback acknowledges a single published, successfully persisted command, never a pointer press. */
internal fun acceptedFeedback(before: GameRecord, after: GameUiState, action: Action): Boolean {
    val next = after.active ?: return false
    return action.type in setOf("HIT", "SCRATCH", "DONE") && after.error == null &&
        next.gameId == before.gameId && next.events.size == before.events.size + 1 &&
        next.events.dropLast(1) == before.events && next.events.last().action == action
}
