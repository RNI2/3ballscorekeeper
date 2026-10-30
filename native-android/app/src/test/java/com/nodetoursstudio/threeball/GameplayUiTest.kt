package com.nodetoursstudio.threeball

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    @After fun cleanup() { store.clear() }
    private fun launch() {
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
        compose.setContent { ThreeBallApp(model) }
        compose.waitForIdle()
    }
    @Test fun noviceCanScoreScratchUndoAndArchiveFromActualControls() {
        launch()
        compose.onNodeWithText("Start Game").performScrollTo().performClick()
        compose.onNodeWithText("HIT +1").performClick()
        compose.onNodeWithText("Scratch +1").performClick()
        compose.onNodeWithText("Undo Scratch").performClick()
        compose.onNodeWithText("Done →").performClick()
        compose.onNodeWithText("HIT +1").performClick()
        compose.onNodeWithText("Done →").performClick()
        compose.onNodeWithText("Finish Game").performClick()
        compose.onNodeWithText("Confirm").performClick()
        compose.onNodeWithText("History", substring = true).performClick()
        compose.onNodeWithText("Tie: Player 1, Player 2").assertExists()
        compose.onNodeWithText("Sign in").assertDoesNotExist()
    }
    @Test fun masterClearAndFinalReviewArePlayable() {
        launch()
        compose.onNodeWithText("Master").performClick()
        compose.onNodeWithText("Rounds (1–999, blank = unlimited)").performTextInput("1")
        compose.onNodeWithText("Start Game").performScrollTo().performClick()
        repeat(2) {
            compose.onNodeWithText("3").performClick()
            compose.onNodeWithText("Done →").performClick()
        }
        compose.onNodeWithText("Final round complete — review scores and finish.").assertExists()
        compose.onNodeWithText("Finish Game").performClick()
        compose.onNodeWithText("Confirm").performClick()
        compose.onNodeWithText("History", substring = true).performClick()
        compose.onNodeWithText("Tie: Player 1, Player 2").assertExists()
    }
}
