package com.nodetoursstudio.threeball.ui

import android.animation.ValueAnimator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.nodetoursstudio.threeball.R
import kotlin.math.cos
import kotlin.math.sin

internal object TableColors {
    val Deep = Color(0xFF032F28)
    val Felt = Color(0xFF07352D)
    val Raised = Color(0xFF105448)
    val Teal = Color(0xFF125548)
    val Pressed = Color(0xFF087453)
    val Cream = Color(0xFFFFF8E8)
    val Gold = Color(0xFFF0D28B)
    val Copper = Color(0xFF8B451F)
    val CopperLight = Color(0xFFB77643)
    // Slightly darker than the suggested action green to retain ivory-text contrast.
    val Green = Color(0xFF08754D)
    val Blue = Color(0xFF075B8A)
    val Red = Color(0xFFB52323)
    val Muted = Color(0xFFC6DCD4)
    val Ink = Color(0xFF011A16)
}
internal object TableSpace { val Gutter = 12.dp; val Gap = 6.dp; val Shape = RoundedCornerShape(16.dp) }
internal val LocalTactileMotion = staticCompositionLocalOf { true }
internal val TactilePressed = SemanticsPropertyKey<Boolean>("TactilePressed")
internal val TactileScale = SemanticsPropertyKey<Float>("TactileScale")
@Composable private fun motionEnabled() = LocalTactileMotion.current && ValueAnimator.areAnimatorsEnabled()

@Composable
internal fun ThreeBallTheme(content: @Composable () -> Unit) {
    val base = Typography()
    val type = base.copy(
        titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
        bodyMedium = base.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
        labelSmall = base.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp, letterSpacing = 1.sp))
    MaterialTheme(colorScheme = darkColorScheme(primary = TableColors.Gold, onPrimary = TableColors.Ink,
        secondary = TableColors.Green, background = TableColors.Felt, surface = TableColors.Raised,
        onBackground = TableColors.Cream, onSurface = TableColors.Cream, surfaceVariant = TableColors.Teal,
        onSurfaceVariant = TableColors.Muted, outline = Color(0xFF64877B), error = Color(0xFFFFB4AB)), typography = type) {
        CompositionLocalProvider(LocalContentColor provides TableColors.Cream, LocalTextStyle provides type.bodyMedium, content = content)
    }
}
internal fun Modifier.felt(): Modifier = background(Brush.horizontalGradient(listOf(TableColors.Ink, TableColors.Deep, TableColors.Felt, TableColors.Deep))).drawBehind {
    var x = 0f
    while (x < size.width) { drawLine(Color.White.copy(alpha = .013f), Offset(x, 0f), Offset(x, size.height), .5.dp.toPx()); x += 5.dp.toPx() }
}

@Composable
internal fun ThreeBallLogo(size: Dp = 38.dp) {
    val diameter = with(LocalDensity.current) { size.toPx() }
    Box(Modifier.size(size).clearAndSetSemantics {}.shadow(4.dp, CircleShape).clip(CircleShape)
        .background(Brush.radialGradient(listOf(Color(0xFFFF8B7F), Color(0xFFD52220), Color(0xFF65080E)), center = Offset(diameter*.28f, diameter*.2f), radius = diameter))
        .border(1.dp, Color(0xFF5B0707), CircleShape).drawBehind {
            drawCircle(Color.White.copy(alpha = .4f), this.size.width*.065f, Offset(this.size.width*.28f,this.size.height*.21f))
        }, contentAlignment = Alignment.Center) {
        Box(Modifier.size(size*.46f).clip(CircleShape).background(Brush.verticalGradient(listOf(TableColors.Cream, Color(0xFFE2D6B6)))), contentAlignment = Alignment.Center) {
            Text("3", color = TableColors.Ink, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Black,
                fontSize = (size.value*.32f/LocalDensity.current.fontScale).sp, lineHeight = (size.value*.4f/LocalDensity.current.fontScale).sp)
        }
    }
}
@Composable
internal fun BrandedHeader() {
    Row(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(TableColors.Ink, TableColors.Deep))).padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        ThreeBallLogo()
        Column(Modifier.weight(1f)) {
            Text("3-Ball Scorekeeper", style = MaterialTheme.typography.titleLarge, fontSize = 21.sp, lineHeight = 25.sp)
            if (LocalDensity.current.fontScale < 1.5f) Text(stringResource(R.string.studio_name).uppercase(), color = TableColors.Muted, fontSize = 9.sp, lineHeight = 12.sp, letterSpacing = 1.5.sp)
        }
    }
    HorizontalDivider(thickness = 2.dp, color = TableColors.Copper)
    HorizontalDivider(thickness = .5.dp, color = TableColors.CopperLight)
}
@Composable
internal fun RailSection(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().shadow(3.dp, TableSpace.Shape).clip(TableSpace.Shape)
        .background(Brush.linearGradient(listOf(Color(0xFF24170E), TableColors.Copper, Color(0xFF422313), TableColors.CopperLight)))
        .padding(3.dp).border(.5.dp, TableColors.CopperLight, RoundedCornerShape(13.dp))
        .clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(listOf(TableColors.Ink, TableColors.Felt)))) {
        Text(title, color = TableColors.Gold, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
            fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.3.sp,
            modifier = Modifier.fillMaxWidth().background(TableColors.Ink.copy(alpha = .5f)).padding(horizontal = 11.dp, vertical = 8.dp).semantics { heading() })
        HorizontalDivider(color = TableColors.CopperLight.copy(alpha = .55f), thickness = .5.dp)
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(TableSpace.Gap), content = content)
    }
}
@Composable
internal fun FeltCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp))
        .background(Brush.linearGradient(listOf(TableColors.Raised, TableColors.Felt)))
        .border(.5.dp, TableColors.Cream.copy(alpha = .16f), RoundedCornerShape(12.dp)).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp), content = content)
}
@Composable
internal fun SelectionPanel(selected: Boolean, onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val duration = if (motionEnabled()) 120 else 0
    val edge by animateColorAsState(if (selected) TableColors.Gold.copy(alpha = .8f) else TableColors.Cream.copy(alpha = .2f), tween(duration), label = "selection edge")
    val shape = RoundedCornerShape(12.dp)
    Column(modifier.fillMaxWidth().shadow(if (pressed) 0.dp else 2.dp, shape).clip(shape)
        .background(Brush.linearGradient(if (selected || pressed) listOf(TableColors.Pressed, TableColors.Felt) else listOf(TableColors.Raised, TableColors.Felt)))
        .border(if (selected) 1.5.dp else .5.dp, edge, shape)
        .clickable(interactionSource = source, indication = ripple(), enabled = enabled, role = Role.RadioButton, onClick = onClick)
        .semantics { this.selected = selected }.heightIn(min = 56.dp).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp), content = content)
}
@Composable
internal fun PrimaryScoreButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    TactileActionButton(label, onClick, modifier, enabled, TableColors.Green, 60.dp)
@Composable
internal fun SecondaryActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, highlighted: Boolean = false) =
    TactileActionButton(label, onClick, modifier, enabled, if (highlighted) TableColors.Pressed else TableColors.Teal, 48.dp, highlighted)
@Composable
internal fun SupportActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    TactileActionButton(label, onClick, modifier, enabled, TableColors.Blue, 48.dp)
@Composable
internal fun DestructiveActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    TactileActionButton(label, onClick, modifier, enabled, TableColors.Red.copy(alpha = .78f), 48.dp)

@Composable
internal fun TactileActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
                                color: Color = TableColors.Green, minimum: Dp = 60.dp, highlighted: Boolean = false) {
    val source = remember { MutableInteractionSource() }
    val down by source.collectIsPressedAsState()
    val pressed = enabled && down
    val motion = motionEnabled()
    val duration = if (motion) 100 else 0
    val scale by animateFloatAsState(if (pressed && motion) .98f else 1f, tween(duration), label = "button depression")
    val elevation by animateDpAsState(if (pressed || !enabled) 0.dp else 4.dp, tween(duration), label = "button elevation")
    val base = if (!enabled) Color(0xFF304B41) else if (pressed) lerp(color, TableColors.Ink, .2f) else color
    Surface(onClick = onClick, enabled = enabled, interactionSource = source,
        modifier = modifier.semantics { role = Role.Button; this[TactilePressed] = pressed; this[TactileScale] = scale }, shape = RoundedCornerShape(11.dp),
        color = Color.Transparent, border = BorderStroke(if (highlighted) 1.5.dp else .7.dp,
            if (enabled) lerp(color, TableColors.Cream, .35f) else Color(0xFF536C61)), shadowElevation = elevation) {
        Box(Modifier.heightIn(min = minimum).background(Brush.verticalGradient(listOf(lerp(base, Color.White, if (enabled) .06f else 0f), base, lerp(base, TableColors.Ink, .35f))))
            .drawBehind { drawLine(Color.White.copy(alpha = if (enabled) .18f else .04f), Offset(10.dp.toPx(),1.dp.toPx()), Offset(size.width-10.dp.toPx(),1.dp.toPx()), .7.dp.toPx()) }
            .padding(horizontal = 12.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            // Only the artwork depresses; the clickable surface retains its full minimum touch target.
            Text(label, modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp,
                color = if (enabled) TableColors.Cream else Color(0xFFB7C7BD), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
@Composable
internal fun PlayerScoreCard(name: String, score: Int, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(TableColors.Raised, TableColors.Felt)))
        .border(.7.dp, TableColors.Cream.copy(alpha = .24f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(min = 64.dp).semantics(mergeDescendants = true) {
            contentDescription = "$name score $score"; liveRegion = LiveRegionMode.Polite
        }) {
            Text("SCORE", color = TableColors.Muted, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text("$score", style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum", fontSize = 42.sp, lineHeight = 46.sp, fontWeight = FontWeight.Black))
        }
    }
}
@Composable
internal fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(9.dp)).background(Brush.verticalGradient(listOf(TableColors.Teal, TableColors.Felt)))
        .border(.5.dp, TableColors.Cream.copy(alpha = .14f), RoundedCornerShape(9.dp)).padding(horizontal = 6.dp, vertical = 6.dp)
        .semantics(mergeDescendants = true) {}, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold, color = TableColors.Muted)
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.SansSerif, fontFeatureSettings = "tnum", fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.ExtraBold))
    }
}
@Composable
internal fun ThreeBallBottomBar(page: String, history: Boolean, select: (String) -> Unit) {
    Column(Modifier.background(TableColors.Ink)) {
        HorizontalDivider(color = TableColors.CopperLight.copy(alpha = .35f), thickness = .5.dp)
        Row(Modifier.fillMaxWidth().navigationBarsPadding(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOfNotNull("Play", "History".takeIf { history }, "Settings").forEach { label ->
                val active = page == label
                Column(Modifier.weight(1f).padding(horizontal = 6.dp, vertical = 3.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (active) TableColors.Raised else Color.Transparent).clickable(role = Role.Tab) { select(label) }
                    .semantics { selected = active }.heightIn(min = 52.dp).padding(vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(Modifier.size(19.dp)) {
                        val c = if (active) TableColors.Gold else TableColors.Muted
                        when (label) {
                            "Play" -> { drawRoundRect(c, cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()));drawLine(c,Offset(size.width*.28f,size.height*.7f),Offset(size.width*.72f,size.height*.3f),2.dp.toPx()) }
                            "History" -> { drawCircle(c,style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()));drawLine(c,center,Offset(size.width*.5f,size.height*.2f),1.5.dp.toPx());drawLine(c,center,Offset(size.width*.75f,size.height*.6f),1.5.dp.toPx()) }
                            else -> {
                                drawCircle(c,radius=size.width*.32f,style=androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()))
                                drawCircle(c,radius=size.width*.1f,style=androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()))
                                repeat(8) { val angle=it*Math.PI/4;drawLine(c,Offset(center.x+cos(angle).toFloat()*size.width*.35f,center.y+sin(angle).toFloat()*size.height*.35f),Offset(center.x+cos(angle).toFloat()*size.width*.49f,center.y+sin(angle).toFloat()*size.height*.49f),2.dp.toPx()) }
                            }
                        }
                    }
                    Text(label, fontSize = 11.sp, lineHeight = 14.sp, color = if (active) TableColors.Cream else TableColors.Muted, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}
@Composable
internal fun PlayerRosterCard(heading: String, status: String, leader: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(TableColors.Raised,TableColors.Felt)))) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(if (leader) TableColors.Gold else TableColors.Blue))
        Column(Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(status, style = MaterialTheme.typography.labelSmall, color = TableColors.Muted)
            Text(heading, fontWeight = FontWeight.Bold)
            content()
        }
    }
}
