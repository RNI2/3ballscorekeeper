package com.nodetoursstudio.threeball

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.nodetoursstudio.threeball.ui.*
import com.nodetoursstudio.threeball.records.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h800dp", application = Application::class)
class HistoryPolishTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @Test fun expandingAnotherGameClosesThePreviousDetails() {
        val games = listOf(reviewRecord(finished = true), reviewRecord(finished = true))
        compose.setContent { ThreeBallTheme { HistoryScreen(games, 0) } }
        compose.onAllNodesWithText("Round details")[0].performScrollTo().performClick()
        compose.onAllNodesWithText("Hide details").assertCountEquals(1)
        compose.onNodeWithText("Round details").performScrollTo().performClick()
        compose.onAllNodesWithText("Hide details").assertCountEquals(1)
        compose.onAllNodesWithText("Round details").assertCountEquals(1)
    }
    @Test fun rankingColumnsAndSelectedRoundNeverMutateResult() {
        val game = Records.apply(reviewSixPlayerRecord(), Action("FINISH"), 1793383200002)
        val snapshot = RecordJson.encode(game)
        compose.setContent { ThreeBallTheme { HistoryScreen(listOf(game), 0) } }
        compose.onNodeWithText("Round details").performScrollTo().performClick()
        compose.onNodeWithText("AVG").assertExists()
        compose.onNodeWithText("BEST").assertExists()
        compose.onNodeWithText("SCR").assertExists()
        compose.onNodeWithText("Round 2").performScrollTo().performClick().assertIsSelected()
        compose.onNodeWithText("ROUND 2").assertExists()
        compose.onNodeWithText("Next →").assertIsNotEnabled()
        compose.onNodeWithText("← Previous").performScrollTo().performClick()
        compose.onNodeWithText("Round 1").assertIsSelected()
        assertEquals(snapshot, RecordJson.encode(game))
    }
}
