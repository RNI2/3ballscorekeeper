package com.nodetoursstudio.threeball

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelStore
import com.nodetoursstudio.threeball.data.*
import com.nodetoursstudio.threeball.records.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h800dp", application = Application::class)
class GameplayUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val store = ViewModelStore()
    @After fun cleanup() { store.clear() }
    private fun launch(fontScale: Float = 1f) {
        val repo = object : LocalGameRepository {
            var active: GameRecord? = null
            val history = mutableListOf<GameRecord>()
            override suspend fun load() = SavedGames(active, history.toList())
            override suspend fun save(next: GameRecord, previous: GameRecord?) { active = next }
            override suspend fun finish(next: GameRecord, previous: GameRecord) { if (next.result != null) history.add(next); active = null }
            override suspend fun abandon(previous: GameRecord) { active = null }
        }
        val prefs = object : PreferencesRepository {
            override val preferences = MutableStateFlow(GamePreferences())
            override suspend fun setKeepScreenAwake(value: Boolean) { preferences.value = GamePreferences(value) }
        }
        lateinit var model: GameViewModel
        compose.runOnIdle { model = GameViewModel(repo, prefs, { 1000L }, Dispatchers.Main.immediate); store.put("ui", model) }
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) { ThreeBallApp(model) }
        }
        compose.waitForIdle()
    }
    @Test fun noviceCanScoreScratchUndoAndArchiveFromActualControls() {
        launch()
        compose.onNodeWithText("Start Game").performScrollTo().performClick()
        compose.onNodeWithText("HIT +1").performScrollTo().performClick()
        compose.onNodeWithText("Scratch +1").performScrollTo().performClick()
        compose.onNodeWithText("Undo Scratch").performScrollTo().performClick()
        compose.onNodeWithText("Done →").performScrollTo().performClick()
        compose.onNodeWithText("HIT +1").performScrollTo().performClick()
        compose.onNodeWithText("Done →").performScrollTo().performClick()
        compose.onNodeWithText("Finish Game").performScrollTo().performClick()
        compose.onNodeWithText("Confirm").performClick()
        compose.onNodeWithText("History", substring = true).performClick()
        compose.onNodeWithText("Tie: Player 1, Player 2").assertExists()
        compose.onNodeWithText("Sign in").assertDoesNotExist()
    }
    @Test fun masterClearAndFinalReviewArePlayable() {
        launch()
        compose.onNodeWithText("Master").performScrollTo().performClick()
        compose.onNodeWithText("Rounds (1–999, blank = unlimited)").performScrollTo().performTextInput("1")
        compose.onNodeWithText("Start Game").performScrollTo().performClick()
        repeat(2) {
            compose.onNodeWithText("Hit").performScrollTo().performClick()
            compose.onNodeWithText("3").performScrollTo().performClick()
            compose.onNodeWithText("Done →").performScrollTo().performClick()
        }
        compose.onNodeWithText("Final round complete — review scores and finish.").assertExists()
        compose.onNodeWithText("Finish Game").performScrollTo().performClick()
        compose.onNodeWithText("Confirm").performClick()
        compose.onNodeWithText("History", substring = true).performClick()
        compose.onNodeWithText("Tie: Player 1, Player 2").assertExists()
    }
    @Test fun openingAndCancelingMasterChooserDoesNotRecordHit() {
        launch()
        compose.onNodeWithText("Master").performScrollTo().performClick()
        compose.onNodeWithText("Start Game").performScrollTo().performClick()
        compose.onNodeWithText("Hit").performScrollTo().performClick()
        compose.onNodeWithText("How many balls went in?").assertExists()
        compose.onNodeWithText("Cancel").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Player 1 score 0").assertExists()
        compose.onNodeWithText("Scorecard / Players").performScrollTo().performClick()
        compose.onNodeWithText("Return to scoring").assertExists()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Hit").assertExists()
        compose.onNodeWithContentDescription("Player 1 score 0").assertExists()
    }

    @Test fun proAndExpertRetainStandardControls() {
        launch()
        compose.onNodeWithText("Pro").performScrollTo().performClick()
        compose.onNodeWithText("Hits per turn (1–99)").assertExists()
        compose.onNodeWithText("Expert").performScrollTo().performClick()
        compose.onNodeWithText("Start Game").performScrollTo().performClick()
        repeat(5) { compose.onNodeWithText("HIT +1").performScrollTo().performClick() }
        compose.onNodeWithText("HIT +1").assertIsNotEnabled()
        compose.onNodeWithText("Scratch +1").performScrollTo().performClick()
        compose.onNodeWithText("Scratch +1").assertIsNotEnabled()
        compose.onNodeWithText("Undo Last Hit").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Player 1 score 5").assertExists()
    }
    @Test @Config(qualifiers = "w360dp-h640dp") fun shortPhoneKeepsActionsReachable() {
        launch()
        compose.onNodeWithText("Start Game").performScrollTo().performClick()
        compose.onNodeWithText("HIT +1").performScrollTo().performClick()
        compose.onNodeWithText("Done →").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Scorecard / Players").performScrollTo().assertIsDisplayed()
    }

    @Test fun proCustomCapIsPlayable() {
        launch()
        compose.onNodeWithText("Pro").performScrollTo().performClick()
        compose.onNodeWithText("Hits per turn (1–99)").performScrollTo().performTextReplacement("1")
        compose.onNodeWithText("Start Game").performScrollTo().performClick()
        compose.onNodeWithText("HIT +1").performScrollTo().performClick()
        compose.onNodeWithText("HIT +1").assertIsNotEnabled()
        compose.onNodeWithText("Scratch +1").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Player 1 score 2").assertExists()
        compose.onNodeWithText("Done →").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Player 2 score 0").assertExists()
    }
    @Test fun masterScratchUndoAndSavedTurnCorrection() {
        launch()
        compose.onNodeWithText("Master").performScrollTo().performClick()
        compose.onNodeWithText("Start Game").performScrollTo().performClick()
        compose.onNodeWithText("Hit").performScrollTo().performClick()
        compose.onNodeWithText("2").performScrollTo().performClick()
        compose.onNodeWithText("Scratch last hit +1").performScrollTo().performClick()
        compose.onNodeWithText("Undo Last Hit").performScrollTo().performClick()
        compose.onNodeWithText("0/6").assertExists()
        compose.onNodeWithText("Hit").performScrollTo().performClick()
        compose.onNodeWithText("3").performScrollTo().performClick()
        compose.onNodeWithText("Done →").performScrollTo().performClick()
        compose.onNodeWithText("Scorecard / Players").performScrollTo().performClick()
        compose.onNodeWithText("Edit Player 1 · round 1").performScrollTo().performClick()
        compose.onNodeWithText("Confirm").performClick()
        compose.onNodeWithContentDescription("Player 1 score -2").assertExists()
        compose.onNodeWithText("Scratch last hit +1").performScrollTo().performClick()
        compose.onNodeWithText("Done →").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Player 2 score 0").assertExists()
    }
    @Test fun negativeScoreAndActionsRemainReachableAtDoubleFontSize() {
        launch(2f)
        compose.onNodeWithText("Master").performScrollTo().performClick()
        compose.onNodeWithText("Start Game").performScrollTo().performClick()
        compose.onNodeWithText("Hit").performScrollTo().performClick()
        compose.onNodeWithText("3").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Player 1 score -2").assertExists()
        compose.onNodeWithText("Done →").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Scorecard / Players").performScrollTo().assertIsDisplayed()
    }

}
