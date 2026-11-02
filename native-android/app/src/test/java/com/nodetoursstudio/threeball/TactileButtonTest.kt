package com.nodetoursstudio.threeball

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.nodetoursstudio.threeball.ui.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class TactileButtonTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @Test fun pressReleaseProducesOneClickAndNoAnimationGeneratedClicks() {
        var clicks = 0
        compose.setContent { ThreeBallTheme { PrimaryScoreButton("Hit", { clicks++ }) } }
        compose.onNodeWithText("Hit").performTouchInput { down(center) }
        compose.onNodeWithText("Hit").assert(SemanticsMatcher.expectValue(TactilePressed, true))
        compose.runOnIdle { assertEquals(0, clicks) }
        compose.onNodeWithText("Hit").performTouchInput { up() }
        compose.onNodeWithText("Hit").assert(SemanticsMatcher.expectValue(TactilePressed, false))
        compose.runOnIdle { assertEquals(1, clicks) }
    }
    @Test fun disabledButtonCannotDepressOrClick() {
        var clicks = 0
        compose.setContent { ThreeBallTheme { PrimaryScoreButton("Hit", { clicks++ }, enabled = false) } }
        compose.onNodeWithText("Hit").performTouchInput { down(center); up() }
        compose.onNodeWithText("Hit").assertIsNotEnabled().assert(SemanticsMatcher.expectValue(TactilePressed, false))
        compose.runOnIdle { assertEquals(0, clicks) }
    }
    @Test fun reducedMotionKeepsScaleStationaryWithoutBlockingClick() {
        var clicks = 0
        compose.setContent { ThreeBallTheme { CompositionLocalProvider(LocalTactileMotion provides false) { PrimaryScoreButton("Hit", { clicks++ }) } } }
        compose.onNodeWithText("Hit").performTouchInput { down(center) }
        compose.onNodeWithText("Hit").assert(SemanticsMatcher.expectValue(TactileScale, 1f))
        compose.onNodeWithText("Hit").performTouchInput { up() }
        compose.runOnIdle { assertEquals(1, clicks) }
    }
}
