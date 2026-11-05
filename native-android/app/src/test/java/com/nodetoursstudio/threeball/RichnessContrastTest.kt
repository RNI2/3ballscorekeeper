package com.nodetoursstudio.threeball

import androidx.compose.ui.graphics.Color
import com.nodetoursstudio.threeball.ui.TableColors
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.pow

/** Semantic base-color checks, not a claim that every gradient/device pixel has been audited. */
class RichnessContrastTest {
    private fun luminance(c: Color): Double {
        fun linear(v: Float): Double = if (v <= .04045f) v.toDouble()/12.92 else ((v+.055)/1.055).pow(2.4)
        return .2126*linear(c.red)+.7152*linear(c.green)+.0722*linear(c.blue)
    }
    private fun contrast(a: Color, b: Color): Double {
        val x=luminance(a); val y=luminance(b)
        return (maxOf(x,y)+.05)/(minOf(x,y)+.05)
    }
    @Test fun enabledActionBaseColorsSupportIvoryLabels() {
        listOf(TableColors.Green,TableColors.Blue,TableColors.Red,TableColors.Neutral,TableColors.Confirm).forEach {
            assertTrue("Base action label contrast must be at least 4.5",contrast(TableColors.Cream,it)>=4.5)
        }
    }
    @Test fun helpTextRemainsReadableOnNeutralSurfaces() {
        listOf(TableColors.Canvas,TableColors.CanvasRaised,TableColors.Panel,TableColors.Disabled).forEach {
            assertTrue("Secondary label contrast must be at least 4.5",contrast(TableColors.Muted,it)>=4.5)
        }
    }
    @Test fun emeraldIsReservedForEmphasisRatherThanThePageCanvas() {
        fun chroma(c: Color)=maxOf(c.red,c.green,c.blue)-minOf(c.red,c.green,c.blue)
        assertTrue(chroma(TableColors.Canvas)<.1f)
        assertTrue(chroma(TableColors.Felt)>chroma(TableColors.Canvas)*3)
        assertTrue(chroma(TableColors.Panel)<chroma(TableColors.Felt))
        assertNotEquals(TableColors.Felt,TableColors.Canvas)
    }
}
