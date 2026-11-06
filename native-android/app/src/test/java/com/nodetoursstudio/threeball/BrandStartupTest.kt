package com.nodetoursstudio.threeball

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.util.TypedValue
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BrandStartupTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private fun render(id: Int, width: Int = 96, height: Int = 96): Bitmap {
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        context.getDrawable(id)!!.apply { setBounds(0, 0, width, height); draw(Canvas(image)) }
        return image
    }
    @Test fun sharedBallHasTransparentCornersAndVisibleCenter() {
        val image = render(R.drawable.ic_three_ball)
        assertEquals(0, Color.alpha(image.getPixel(0, 0)))
        assertEquals(255, Color.alpha(image.getPixel(48, 48)))
    }
    @Test fun legacyLaunchUsesDarkCanvasAndCenteredMark() {
        val image = render(R.drawable.startup_background, 360, 800)
        assertEquals(Color.rgb(18, 27, 30), image.getPixel(0, 0))
        assertNotEquals(image.getPixel(0, 0), image.getPixel(180, 400))
    }
    @Test fun launcherUsesAdaptiveLayersWithoutWhiteBacking() {
        assertTrue(context.getDrawable(R.mipmap.ic_launcher) is AdaptiveIconDrawable)
        val image = render(R.mipmap.ic_launcher)
        assertNotEquals(Color.WHITE, image.getPixel(48, 12))
    }
    @Test @Config(sdk = [31]) fun android12SplashUsesExplicitBrandResources() {
        val theme = context.resources.newTheme().apply { applyStyle(R.style.Theme_ThreeBall, true) }
        fun value(attribute: Int) = TypedValue().also { assertTrue(theme.resolveAttribute(attribute, it, true)) }
        assertEquals(Color.rgb(18, 27, 30), value(android.R.attr.windowSplashScreenBackground).data)
        assertEquals(Color.rgb(18, 27, 30), value(android.R.attr.windowSplashScreenIconBackgroundColor).data)
        assertEquals(R.drawable.studio_header, value(android.R.attr.windowSplashScreenBrandingImage).resourceId)
        assertEquals(R.drawable.brand_splash_icon, value(android.R.attr.windowSplashScreenAnimatedIcon).resourceId)
    }
}
