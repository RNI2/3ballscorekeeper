package com.nodetoursstudio.threeball.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nodetoursstudio.threeball.GameUiState
import com.nodetoursstudio.threeball.engine.*
import com.nodetoursstudio.threeball.records.*

/** Local sample data only. These previews call the same production presentation components. */
internal fun reviewRecord(afterHit: Boolean = false, finished: Boolean = false): GameRecord {
    var record = Records.start(GameConfig(Difficulty.MASTER, ScratchPlacement.BALL_IN_HAND, roundLimit = 3), listOf("Alex", "Jordan"), 1793383200000)
    if (afterHit || finished) record = Records.apply(record, Action("HIT", 3), 1793383200001)
    if (finished) {
        record = Records.apply(record, Action("DONE"), 1793383200002)
        record = Records.apply(record, Action("HIT", 3), 1793383200003)
        record = Records.apply(record, Action("DONE"), 1793383200004)
        record = Records.apply(record, Action("FINISH"), 1793383200005)
    }
    return record
}
@Composable
internal fun ReviewFrame(page: String = "Play", content: @Composable () -> Unit) {
    ThreeBallTheme {
        Column(Modifier.fillMaxSize().felt()) {
            BrandedHeader()
            Box(Modifier.weight(1f)) { content() }
            ThreeBallBottomBar(page, true) {}
        }
    }
}
@Preview(name = "Setup · 360 × 800", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
internal fun SetupReview() = ReviewFrame { SetupScreen(false) { _, _ -> } }
@Preview(name = "Master · ready", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
internal fun IdleReview() = ReviewFrame { val r = reviewRecord(); LiveScreen(GameUiState(busy = false, loaded = true, active = r, game = Records.state(r)), {}, {}) }
@Preview(name = "Master · chooser", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
internal fun ChooserReview() = ReviewFrame { val r = reviewRecord(); LiveScreen(GameUiState(busy = false, loaded = true, active = r, game = Records.state(r)), {}, {}, initialChooser = true) }
@Preview(name = "Master · negative score", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
internal fun AfterHitReview() = ReviewFrame { val r = reviewRecord(true); LiveScreen(GameUiState(busy = false, loaded = true, active = r, game = Records.state(r)), {}, {}) }
@Preview(name = "Scorecard · tie", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
internal fun ScorecardReview() = ReviewFrame {
    val r = reviewRecord(finished = true).let { it.copy(events = it.events.dropLast(1), status = "ACTIVE", result = null, completedAtEpochMillis = null) }
    LiveScreen(GameUiState(busy = false, loaded = true, active = r, game = Records.state(r)), {}, {}, initialScorecard = true)
}
@Preview(name = "History", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
internal fun HistoryReview() = ReviewFrame("History") { HistoryScreen(listOf(reviewRecord(finished = true)), 0) }
@Preview(name = "Master · 200% font", widthDp = 360, heightDp = 800, fontScale = 2f, showBackground = true)
@Composable
internal fun LargeFontReview() = AfterHitReview()
@Preview(name = "Master · short phone", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
internal fun SmallReview() = AfterHitReview()
@Preview(name = "Master · landscape", widthDp = 800, heightDp = 360, showBackground = true)
@Composable
internal fun LandscapeReview() = AfterHitReview()
@Preview(name = "Master · tall phone", widthDp = 400, heightDp = 900, showBackground = true)
@Composable
internal fun TallReview() = AfterHitReview()

@Preview(name = "Novice", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
internal fun NoviceReview() = ReviewFrame {
    val r = Records.start(GameConfig(Difficulty.NOVICE, ScratchPlacement.KITCHEN), listOf("Alex", "Jordan"), 1793383200000)
    LiveScreen(GameUiState(busy = false, loaded = true, active = r, game = Records.state(r)), {}, {})
}
@Preview(name = "Settings", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
internal fun SettingsReview() = ReviewFrame("Settings") { SettingsScreen(GameUiState(busy = false, loaded = true)) {} }

internal fun reviewSixPlayerRecord(): GameRecord {
    var r = Records.start(GameConfig(Difficulty.MASTER, ScratchPlacement.BALL_IN_HAND, roundLimit = 3),
        listOf("Alexandra Montgomerie", "Jordan", "Casey", "Morgan", "Sam", "Riley"), 1793383200000)
    repeat(2) { round -> repeat(6) { player ->
        if ((round + player) % 2 == 0) {
            r = Records.apply(r, Action("HIT", 2), 1793383200001)
            r = Records.apply(r, Action("SCRATCH"), 1793383200001)
            r = Records.apply(r, Action("HIT", 1), 1793383200001)
        } else r = Records.apply(r, Action("HIT", 3), 1793383200001)
        r = Records.apply(r, Action("DONE"), 1793383200001)
    } }
    return r
}
@Preview(name = "History · rankings and rounds", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
internal fun ExpandedHistoryReview() = ReviewFrame("History") {
    val r = Records.apply(reviewSixPlayerRecord(), Action("FINISH"), 1793383200002)
    HistoryScreen(listOf(r), 0, r.gameId)
}
@Preview(name = "Six players · long name", widthDp = 420, heightDp = 900, showBackground = true)
@Composable
internal fun SixPlayerReview() = ReviewFrame {
    val r = Records.apply(reviewSixPlayerRecord(), Action("HIT", 3), 1793383200002)
    LiveScreen(GameUiState(busy = false, loaded = true, active = r, game = Records.state(r)), {}, {})
}
@Preview(name = "Master · 130% font", widthDp = 360, heightDp = 800, fontScale = 1.3f, showBackground = true)
@Composable
internal fun MediumFontReview() = AfterHitReview()
