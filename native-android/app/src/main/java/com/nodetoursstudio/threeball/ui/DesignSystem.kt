package com.nodetoursstudio.threeball.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import com.nodetoursstudio.threeball.R
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

internal object TableColors {
    val Deep = Color(0xFF021F1C)
    val Felt = Color(0xFF07352D)
    val Raised = Color(0xFF104C3F)
    val Teal = Color(0xFF125548)
    val Cream = Color(0xFFFFF8E8)
    val Gold = Color(0xFFF0D28B)
    val Copper = Color(0xFF7D3B18)
    val CopperLight = Color(0xFFB76A2C)
    val Green = Color(0xFF11734D)
    val Blue = Color(0xFF1684DC)
    val Red = Color(0xFFB52323)
    val Muted = Color(0xFFB7D0C5)
}
internal object TableSpace { val Gutter = 12.dp; val Gap = 8.dp; val Shape = RoundedCornerShape(14.dp) }

@Composable
internal fun ThreeBallTheme(content: @Composable () -> Unit) {
    val base = Typography()
    MaterialTheme(colorScheme = darkColorScheme(primary = TableColors.Gold, onPrimary = TableColors.Deep,
        secondary = TableColors.Green, background = TableColors.Felt, surface = TableColors.Raised,
        onBackground = TableColors.Cream, onSurface = TableColors.Cream, surfaceVariant = TableColors.Teal,
        onSurfaceVariant = TableColors.Muted, outline = Color(0xFF64877B), error = Color(0xFFFFB4AB)),
        typography = base.copy(titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
            titleMedium = base.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold))) {
        CompositionLocalProvider(LocalContentColor provides TableColors.Cream, content = content)
    }
}

internal fun Modifier.felt(): Modifier = background(Brush.horizontalGradient(listOf(TableColors.Deep, TableColors.Felt, TableColors.Deep))).drawBehind {
    var x = 0f
    while (x < size.width) { drawLine(Color.White.copy(alpha = .016f), Offset(x, 0f), Offset(x, size.height), .5.dp.toPx()); x += 5.dp.toPx() }
}

@Composable
internal fun ThreeBallLogo(size: Dp = 36.dp) {
    Box(Modifier.size(size).clearAndSetSemantics {}.clip(RoundedCornerShape(50)).background(Brush.radialGradient(listOf(Color(0xFFFF7770), Color(0xFFD91C1A), Color(0xFF61060A)))), contentAlignment = Alignment.Center) {
        Box(Modifier.size(size * .48f).clip(RoundedCornerShape(50)).background(TableColors.Cream), contentAlignment = Alignment.Center) {
            Text("3", color = TableColors.Deep, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Black, fontSize = (size.value * .32f / LocalDensity.current.fontScale).sp, lineHeight = (size.value * .4f / LocalDensity.current.fontScale).sp)
        }
    }
}

@Composable
internal fun BrandedHeader() {
    Row(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFF000D0C), TableColors.Deep))).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ThreeBallLogo()
        Column {
            Text("3-Ball Scorekeeper", style = MaterialTheme.typography.titleLarge, fontSize = 21.sp)
            if (LocalDensity.current.fontScale < 1.5f) Text(stringResource(R.string.studio_name).uppercase(), color = TableColors.Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
        }
    }
    HorizontalDivider(thickness = 3.dp, color = TableColors.Copper)
}

@Composable
internal fun RailSection(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().clip(TableSpace.Shape)
        .background(Brush.linearGradient(listOf(TableColors.Deep, TableColors.Raised)))
        .border(2.dp, TableColors.Copper, TableSpace.Shape).padding(4.dp)
        .border(.5.dp, TableColors.CopperLight.copy(alpha = .7f), RoundedCornerShape(10.dp)).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = TableColors.Gold, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.5.sp)
        HorizontalDivider(color = TableColors.Gold.copy(alpha = .35f), thickness = .5.dp)
        content()
    }
}

@Composable
internal fun SelectionPanel(selected: Boolean, onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier,
                            content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(modifier.fillMaxWidth().clip(shape)
        .background(Brush.linearGradient(if (selected) listOf(Color(0xFF126C4D), Color(0xFF065D3C)) else listOf(TableColors.Teal, TableColors.Felt)))
        .border(if (selected) 2.dp else 1.dp, if (selected) Color(0xFF53E299) else Color(0xFF326F60), shape)
        .clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
        .semantics { this.selected = selected }.heightIn(min = 56.dp).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp), content = content)
}

@Composable
internal fun PrimaryScoreButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    TableButton(label, onClick, enabled, modifier, TableColors.Green, 56.dp)
@Composable
internal fun SecondaryActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    TableButton(label, onClick, enabled, modifier, TableColors.Teal, 48.dp)
@Composable
internal fun DestructiveActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    TableButton(label, onClick, enabled, modifier, TableColors.Red.copy(alpha = .7f), 48.dp)

@Composable
private fun TableButton(label: String, onClick: () -> Unit, enabled: Boolean, modifier: Modifier, color: Color, minimum: Dp) {
    Surface(onClick = onClick, enabled = enabled, modifier = modifier.semantics { role = Role.Button }, shape = RoundedCornerShape(11.dp),
        color = Color.Transparent, border = BorderStroke(1.dp, if (enabled) color.copy(alpha = 1f).lerpToCream() else Color(0xFF547367)), shadowElevation = if (enabled) 2.dp else 0.dp) {
        Box(Modifier.background(Brush.verticalGradient(if (enabled) listOf(color, color.copy(red = color.red * .65f, green = color.green * .7f, blue = color.blue * .7f)) else listOf(Color(0xFF304D43), Color(0xFF284137))))
            .heightIn(min = minimum).padding(horizontal = 12.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (enabled) TableColors.Cream else Color(0xFFB0BEB5), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
private fun Color.lerpToCream() = lerp(this, TableColors.Cream, .35f)

@Composable
internal fun PlayerScoreCard(name: String, score: Int, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(TableColors.Teal, TableColors.Felt)))
        .border(1.dp, Color(0xFF477B69), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(min = 64.dp).semantics(mergeDescendants = true) {
                contentDescription = "$name score $score"; liveRegion = LiveRegionMode.Polite
            }) {
                Text("SCORE", color = TableColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("$score", fontSize = 42.sp, fontWeight = FontWeight.Black, lineHeight = 46.sp)
            }
        }
    }
}
@Composable
internal fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(9.dp)).background(TableColors.Teal).padding(horizontal = 6.dp, vertical = 6.dp).semantics(mergeDescendants = true) {}, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TableColors.Muted)
        Text(value, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
    }
}
@Composable
internal fun ThreeBallBottomBar(page: String, history: Boolean, select: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().background(TableColors.Deep).navigationBarsPadding(), horizontalArrangement = Arrangement.SpaceEvenly) {
        listOfNotNull("Play", "History".takeIf { history }, "Settings").forEach { label ->
            Column(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable(role = Role.Tab) { select(label) }
                .semantics { selected = page == label }.heightIn(min = 56.dp).padding(vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(Modifier.size(20.dp)) {
                    val c = if (page == label) TableColors.Gold else TableColors.Muted
                    when (label) {
                        "Play" -> { drawRoundRect(c, cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx())); drawLine(c, Offset(size.width*.28f,size.height*.7f),Offset(size.width*.72f,size.height*.3f),2.dp.toPx()) }
                        "History" -> { drawCircle(c, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()));drawLine(c,center,Offset(size.width*.5f,size.height*.22f),1.5.dp.toPx());drawLine(c,center,Offset(size.width*.75f,size.height*.6f),1.5.dp.toPx()) }
                        else -> { drawCircle(c, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()));drawCircle(c,radius=size.width*.18f) }
                    }
                }
                Text(label, fontSize = 11.sp, color = if (page == label) TableColors.Gold else TableColors.Muted, fontWeight = if (page == label) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
internal fun PlayerRosterCard(heading: String, status: String, leader: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(RoundedCornerShape(12.dp))
        .background(Brush.linearGradient(listOf(TableColors.Teal, TableColors.Felt)))) {
        Box(Modifier.width(4.dp).fillMaxHeight().background(if (leader) TableColors.Green else TableColors.Blue))
        Column(Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(status, color = TableColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(heading, fontWeight = FontWeight.Bold)
            content()
        }
    }
}
