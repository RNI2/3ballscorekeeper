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
    val Canvas = Color(0xFF121B1E)
    val CanvasRaised = Color(0xFF1B2A2E)
    val Panel = Color(0xFF183135)
    val FeltDepth = Color(0xFF093F36)
    val Felt = Color(0xFF0E5C4E)
    val FeltHighlight = Color(0xFF16816A)
    val Neutral = Color(0xFF31454A)
    val Disabled = Color(0xFF344244)
    val Confirm = Color(0xFF0C6657)
    val Cream = Color(0xFFF7F2E7)
    val Gold = Color(0xFFE8BE67)
    val Copper = Color(0xFFB66D43)
    val CopperLight = Color(0xFFB77643)
    val MutedCopper = Color(0xFF754A38)
    val Border = Color(0xFF46585B)
    val Green = Color(0xFF0E7854) // Tuned for ivory label contrast.
    val Blue = Color(0xFF176D9D)
    val Red = Color(0xFFB63839)
    val Muted = Color(0xFFBDD0CD)
    val Ink = Color(0xFF0D1518)
    // Existing presentation names remain aliases; canvas/panels are now deliberately neutral.
    val Deep = Canvas
    val Raised = CanvasRaised
    val Teal = Panel
    val Pressed = FeltDepth
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
        secondary = TableColors.Green, background = TableColors.Canvas, surface = TableColors.Raised,
        onBackground = TableColors.Cream, onSurface = TableColors.Cream, surfaceVariant = TableColors.Teal,
        onSurfaceVariant = TableColors.Muted, outline = TableColors.Border, error = Color(0xFFFFB4AB)), typography = type) {
        CompositionLocalProvider(LocalContentColor provides TableColors.Cream, LocalTextStyle provides type.bodyMedium, content = content)
    }
}
internal fun Modifier.canvas(): Modifier = background(Brush.verticalGradient(listOf(TableColors.Canvas, TableColors.Ink)))
internal fun Modifier.heroFelt(): Modifier = background(Brush.linearGradient(listOf(TableColors.FeltHighlight, TableColors.Felt, TableColors.FeltDepth))).drawBehind {
    var x = 0f
    while (x < size.width) { drawLine(Color.White.copy(alpha = .018f), Offset(x, 0f), Offset(x, size.height), .5.dp.toPx()); x += 4.dp.toPx() }
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
internal fun RailSection(title: String, modifier: Modifier = Modifier, accent: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().shadow(if (accent) 3.dp else 1.dp, TableSpace.Shape).clip(TableSpace.Shape)
        .background(Brush.linearGradient(listOf(TableColors.CanvasRaised, TableColors.Panel)))
        .border(if (accent) 1.dp else .5.dp, if (accent) TableColors.MutedCopper else TableColors.Border.copy(alpha = .65f), TableSpace.Shape)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = TableColors.Gold, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                fontSize = 13.sp, lineHeight = 17.sp, letterSpacing = 1.sp, modifier = Modifier.weight(1f).semantics { heading() })
            Box(Modifier.width(36.dp).height(.5.dp).background(Brush.horizontalGradient(listOf(TableColors.Copper, Color.Transparent))))
        }
        Column(Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(TableSpace.Gap), content = content)
    }
}
@Composable
internal fun FeltCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp))
        .background(Brush.linearGradient(listOf(TableColors.CanvasRaised, TableColors.Panel)))
        .border(.5.dp, TableColors.Border, RoundedCornerShape(12.dp)).padding(10.dp),
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
        .background(Brush.linearGradient(if (selected || pressed) listOf(TableColors.Felt, TableColors.FeltDepth) else listOf(TableColors.CanvasRaised, TableColors.Panel)))
        .border(if (selected) 1.5.dp else .5.dp, edge, shape)
        .clickable(interactionSource = source, indication = ripple(), enabled = enabled, role = Role.RadioButton, onClick = onClick)
        .semantics { this.selected = selected }.heightIn(min = 56.dp).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp), content = content)
}
@Composable
internal fun PrimaryScoreButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    TactileActionButton(label, onClick, modifier, enabled, TableColors.Green, 60.dp, glyph = if (label == "Hit" || label == "HIT +1") ActionGlyph.Hit else if (label == "Start Game") ActionGlyph.Play else null)
@Composable
internal fun SecondaryActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, highlighted: Boolean = false) =
    TactileActionButton(label, onClick, modifier, enabled, if (highlighted) TableColors.Felt else TableColors.Neutral, 48.dp, highlighted, glyph = if (highlighted) ActionGlyph.Check else null)
@Composable
internal fun SupportActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    TactileActionButton(label, onClick, modifier, enabled, TableColors.Blue, 48.dp, glyph = ActionGlyph.Players)
@Composable
internal fun ConfirmationActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    TactileActionButton(label, onClick, modifier, enabled, TableColors.Confirm, 60.dp, glyph = ActionGlyph.Check)
@Composable
internal fun DestructiveActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    TactileActionButton(label, onClick, modifier, enabled, TableColors.Red, 48.dp, glyph = ActionGlyph.Flag)

@Composable
internal fun TactileActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
                                color: Color = TableColors.Green, minimum: Dp = 60.dp, highlighted: Boolean = false, glyph: ActionGlyph? = null) {
    val source = remember { MutableInteractionSource() }
    val down by source.collectIsPressedAsState()
    val pressed = enabled && down
    val motion = motionEnabled()
    val duration = if (motion) 100 else 0
    val scale by animateFloatAsState(if (pressed && motion) .98f else 1f, tween(duration), label = "button depression")
    val elevation by animateDpAsState(if (pressed || !enabled) 0.dp else 4.dp, tween(duration), label = "button elevation")
    val base = if (!enabled) TableColors.Disabled else if (pressed) lerp(color, TableColors.Ink, .2f) else color
    Surface(onClick = onClick, enabled = enabled, interactionSource = source,
        modifier = modifier.semantics { role = Role.Button; this[TactilePressed] = pressed; this[TactileScale] = scale }, shape = RoundedCornerShape(11.dp),
        color = Color.Transparent, border = BorderStroke(if (highlighted) 1.5.dp else .7.dp,
            if (enabled && highlighted) TableColors.Gold else if (enabled) lerp(color, TableColors.Cream, .35f) else TableColors.Border), shadowElevation = elevation) {
        Box(Modifier.heightIn(min = minimum).background(Brush.verticalGradient(listOf(lerp(base, Color.White, if (enabled) .06f else 0f), base, lerp(base, TableColors.Ink, .35f))))
            .drawBehind { drawLine(Color.White.copy(alpha = if (enabled) .18f else .04f), Offset(10.dp.toPx(),1.dp.toPx()), Offset(size.width-10.dp.toPx(),1.dp.toPx()), .7.dp.toPx()) }
            .padding(horizontal = 12.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            // Only the artwork depresses; the clickable surface retains its full minimum touch target.
            Row(Modifier.graphicsLayer { scaleX = scale; scaleY = scale }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                if (glyph != null) ActionIcon(glyph, if (enabled) TableColors.Cream else TableColors.Muted)
                Text(label, modifier = Modifier.weight(1f, fill = false), fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp,
                    color = if (enabled) TableColors.Cream else Color(0xFFB7C7BD), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}
@Composable
internal fun PlayerScoreCard(name: String, score: Int, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).heroFelt()
        .border(1.dp, TableColors.Copper, RoundedCornerShape(12.dp)).padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(name, style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp, lineHeight = 32.sp))
            if (LocalDensity.current.fontScale <= 1.3f && name.length <= 20) BilliardsMotif()
        }
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(min = 68.dp).semantics(mergeDescendants = true) {
            contentDescription = "$name score $score"; liveRegion = LiveRegionMode.Polite
        }) {
            Text("SCORE", color = TableColors.Cream, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text("$score", style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum", fontSize = 48.sp, lineHeight = 52.sp, fontWeight = FontWeight.Black))
        }
    }
}
@Composable
private fun BilliardsMotif() {
    Canvas(Modifier.size(width = 92.dp, height = 16.dp).clearAndSetSemantics {}) {
        drawLine(TableColors.Copper, Offset(0f,size.height*.95f), Offset(size.width*.7f,size.height*.2f), 2.dp.toPx())
        val radius = 6.dp.toPx()
        listOf(Color(0xFFE8B82E), Color(0xFFCB2829), TableColors.Cream).forEachIndexed { i, color ->
            val c = Offset(size.width*.53f + i*14.dp.toPx(), size.height*.55f)
            drawCircle(Brush.radialGradient(listOf(lerp(color,Color.White,.45f),color,lerp(color,Color.Black,.5f)), center = c-Offset(radius*.3f,radius*.3f), radius = radius*1.5f), radius, c)
        }
    }
}
@Composable
internal fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(9.dp)).background(Brush.verticalGradient(listOf(TableColors.CanvasRaised, TableColors.Panel)))
        .border(.5.dp, TableColors.Cream.copy(alpha = .14f), RoundedCornerShape(9.dp)).padding(horizontal = 6.dp, vertical = 6.dp)
        .semantics(mergeDescendants = true) {}, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            StatIcon(label)
            Text(label, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold, color = TableColors.Muted)
        }
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
                    .background(if (active) TableColors.FeltDepth else Color.Transparent).border(if (active) .8.dp else 0.dp, if (active) TableColors.FeltHighlight else Color.Transparent, RoundedCornerShape(10.dp)).clickable(role = Role.Tab) { select(label) }
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
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(TableColors.CanvasRaised,TableColors.Panel)))) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(if (leader) TableColors.Gold else TableColors.Blue))
        Column(Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(status, style = MaterialTheme.typography.labelSmall, color = TableColors.Muted)
            Text(heading, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

internal enum class ActionGlyph { Hit, Play, Check, Players, Flag }
@Composable
internal fun ActionIcon(glyph: ActionGlyph, color: Color) {
    Canvas(Modifier.size(17.dp).clearAndSetSemantics {}) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx())
        when (glyph) {
            ActionGlyph.Hit -> { drawCircle(color, style = stroke); drawCircle(color, radius = size.width*.22f, style = stroke) }
            ActionGlyph.Play -> { val path = Path().apply { moveTo(size.width*.2f,0f); lineTo(size.width,size.height*.5f); lineTo(size.width*.2f,size.height); close() }; drawPath(path,color) }
            ActionGlyph.Check -> { drawLine(color,Offset(0f,size.height*.5f),Offset(size.width*.35f,size.height*.85f),2.dp.toPx());drawLine(color,Offset(size.width*.35f,size.height*.85f),Offset(size.width,size.height*.15f),2.dp.toPx()) }
            ActionGlyph.Players -> { drawCircle(color,radius=size.width*.16f,center=Offset(size.width*.33f,size.height*.23f));drawCircle(color,radius=size.width*.13f,center=Offset(size.width*.75f,size.height*.3f));drawRoundRect(color,topLeft=Offset(size.width*.06f,size.height*.5f),size=androidx.compose.ui.geometry.Size(size.width*.5f,size.height*.45f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()));drawLine(color,Offset(size.width*.7f,size.height*.58f),Offset(size.width*.94f,size.height*.9f),3.dp.toPx()) }
            ActionGlyph.Flag -> { drawLine(color,Offset(size.width*.15f,0f),Offset(size.width*.15f,size.height),1.5.dp.toPx());drawRect(color,topLeft=Offset(size.width*.2f,0f),size=androidx.compose.ui.geometry.Size(size.width*.7f,size.height*.5f)) }
        }
    }
}

@Composable
internal fun StatIcon(label: String) {
    Canvas(Modifier.size(12.dp).clearAndSetSemantics {}) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
        val c = TableColors.Gold
        if (label == "POCKETED") {
            drawRoundRect(c, cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), style = stroke)
            drawCircle(c,radius=size.width*.2f,center=Offset(size.width*.5f,size.height*.32f))
        } else {
            drawCircle(c, style = stroke)
            if (label == "SHOTS") drawCircle(c,radius=size.width*.24f,style=stroke)
            else { drawLine(c,Offset(size.width*.3f,size.height*.3f),Offset(size.width*.7f,size.height*.7f),1.dp.toPx());drawLine(c,Offset(size.width*.3f,size.height*.7f),Offset(size.width*.7f,size.height*.3f),1.dp.toPx()) }
        }
    }
}
@Composable
internal fun DifficultyEmblem(mode: com.nodetoursstudio.threeball.engine.Difficulty) {
    Canvas(Modifier.size(25.dp).clearAndSetSemantics {}) {
        val c = TableColors.Gold
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx())
        when(mode) {
            com.nodetoursstudio.threeball.engine.Difficulty.NOVICE -> {
                val p = Path().apply {moveTo(size.width*.5f,size.height*.08f);lineTo(size.width*.92f,size.height*.9f);lineTo(size.width*.08f,size.height*.9f);close()};drawPath(p,c,style=stroke)
                drawCircle(c,radius=size.width*.07f,center=Offset(size.width*.5f,size.height*.66f))
            }
            com.nodetoursstudio.threeball.engine.Difficulty.PRO -> repeat(3) { i -> drawRect(c,topLeft=Offset(i*size.width*.32f,size.height*(.65f-i*.24f)),size=androidx.compose.ui.geometry.Size(size.width*.2f,size.height*(.35f+i*.24f))) }
            com.nodetoursstudio.threeball.engine.Difficulty.EXPERT -> {
                drawRoundRect(c,topLeft=Offset(size.width*.25f,size.height*.1f),size=androidx.compose.ui.geometry.Size(size.width*.5f,size.height*.55f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()))
                drawLine(c,Offset(size.width*.5f,size.height*.6f),Offset(size.width*.5f,size.height*.9f),2.dp.toPx());drawLine(c,Offset(size.width*.25f,size.height*.92f),Offset(size.width*.75f,size.height*.92f),2.dp.toPx())
                drawCircle(c,radius=size.width*.18f,center=Offset(size.width*.2f,size.height*.3f),style=stroke);drawCircle(c,radius=size.width*.18f,center=Offset(size.width*.8f,size.height*.3f),style=stroke)
            }
            else -> {val p=Path().apply {moveTo(size.width*.12f,size.height*.85f);lineTo(size.width*.03f,size.height*.25f);lineTo(size.width*.3f,size.height*.5f);lineTo(size.width*.5f,size.height*.08f);lineTo(size.width*.7f,size.height*.5f);lineTo(size.width*.97f,size.height*.25f);lineTo(size.width*.88f,size.height*.85f);close()};drawPath(p,c)}
        }
    }
}
