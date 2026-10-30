package com.nodetoursstudio.threeball

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import android.view.View
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.nodetoursstudio.threeball.ui.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w360dp-h800dp", application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UiVisualReviewTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun capture(name: String, content: @Composable () -> Unit) {
        compose.setContent(content)
        compose.waitForIdle()
        val folder = File("build/ui-review").apply { mkdirs() }
        compose.runOnUiThread {
            val view = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            // Draw actual host window roots, including the Master chooser's separate dialog window.
            val managerClass = Class.forName("android.view.WindowManagerGlobal")
            val manager = managerClass.getDeclaredMethod("getInstance").invoke(null)
            val field = managerClass.getDeclaredField("mViews").apply { isAccessible = true }
            @Suppress("UNCHECKED_CAST") val windows = (field.get(manager) as List<View>).toList()
            windows.forEachIndexed { index, window ->
                if (index > 0) canvas.drawColor(android.graphics.Color.argb(90, 0, 0, 0))
                val location = IntArray(2); window.getLocationOnScreen(location)
                canvas.save(); canvas.translate(location[0].toFloat(), location[1].toFloat()); window.draw(canvas); canvas.restore()
            }
            File(folder, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
    @Test fun setup() = capture("setup") { SetupReview() }
    @Test fun masterIdle() = capture("master-idle") { IdleReview() }
    @Test fun masterAfterHit() = capture("master-after-hit") { AfterHitReview() }
    @Test fun scorecard() = capture("scorecard") { ScorecardReview() }
    @Test fun chooser() = capture("master-chooser") { ChooserReview() }
    @Test @Config(qualifiers = "w360dp-h640dp") fun shortPhone() = capture("master-short") { SmallReview() }
    @Test @Config(qualifiers = "w400dp-h900dp") fun tallPhone() = capture("master-tall") { TallReview() }
    @Test @Config(qualifiers = "w800dp-h360dp-land") fun landscape() = capture("master-landscape") { LandscapeReview() }
    @Test fun largeFonts() = capture("master-font-200") {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) { AfterHitReview() }
    }
    @Test fun history() = capture("history") { HistoryReview() }
}
