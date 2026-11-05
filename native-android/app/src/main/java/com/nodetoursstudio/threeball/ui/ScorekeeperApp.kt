package com.nodetoursstudio.threeball.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nodetoursstudio.threeball.*
import com.nodetoursstudio.threeball.records.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.coroutines.flow.first
import android.view.HapticFeedbackConstants

@Composable
internal fun ScorekeeperApp(model: GameViewModel, onAcceptedHaptic: (() -> Unit)? = null) {
    val state by model.ui.collectAsStateWithLifecycle()
    var pending by remember { mutableStateOf(false) }
    val gateScope = rememberCoroutineScope()
    val ui = state.copy(busy = state.busy || pending)
    // Gate the click synchronously, then wait for the unchanged ViewModel command flow to settle.
    fun guarded(onSettled: (GameUiState) -> Unit = {}, block: () -> Unit) {
        if (!state.busy && !pending) {
            pending = true
            gateScope.launch {
                try { block(); yield(); onSettled(model.ui.first { !it.busy }) }
                finally { pending = false }
            }
        }
    }
    var page by rememberSaveable { mutableStateOf("Play") }
    BackHandler(page != "Play") { page = "Play" }
    val view = LocalView.current
    DisposableEffect(view, ui.active != null, ui.preferences.keepScreenAwake) {
        view.keepScreenOn = ui.active != null && ui.preferences.keepScreenAwake
        onDispose { view.keepScreenOn = false }
    }
    ThreeBallTheme {
        Scaffold(modifier = Modifier.imePadding(), containerColor = TableColors.Deep,
            bottomBar = { ThreeBallBottomBar(page, Features.localHistory) { page = it } }) { insets ->
            Column(Modifier.fillMaxSize().canvas().padding(insets)) {
                BrandedHeader()
                if (ui.busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = TableColors.Gold)
                if (ui.recoveredRecords > 0) Text("Unreadable records retained for recovery. See History.", color = TableColors.Gold, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                ui.error?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
                    Row { TextButton(onClick = { guarded { model.reload() } }, enabled = !ui.busy) { Text("Reload saved game") }; TextButton(onClick = model::dismissError) { Text("Dismiss") } }
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    if (!ui.loaded) Column(Modifier.padding(16.dp)) {
                        Text("Opening your local table…")
                        if (!ui.busy) SecondaryActionButton("Retry", { guarded { model.reload() } })
                    } else when (page) {
                        "History" -> HistoryScreen(ui.history, ui.recoveredRecords)
                        "Settings" -> SettingsScreen(ui) { value -> guarded { model.setKeepScreenAwake(value) } }
                        else -> if (ui.active == null) SetupScreen(ui.busy) { config, names -> guarded { model.start(config, names) } }
                            else LiveScreen(ui, { action ->
                                val before = model.ui.value.active
                                guarded(onSettled = { after ->
                                    if (before != null && acceptedFeedback(before, after, action)) {
                                        if (onAcceptedHaptic != null) onAcceptedHaptic() else view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    }
                                }) { model.command(action) }
                            }, { guarded { model.abandon() } })
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingsScreen(ui: GameUiState, awake: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(TableSpace.Gutter), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RailSection("AT THE TABLE") {
            Text("Settings", style = MaterialTheme.typography.titleLarge)
            Text("Keep screen awake during a game")
            Switch(ui.preferences.keepScreenAwake, awake, enabled = !ui.busy, modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Keep screen awake during a game" },
                colors = SwitchDefaults.colors(checkedTrackColor = TableColors.Green))
            Text("Stay focused on the next shot. Your preference is saved locally.", color = TableColors.Muted)
        }
        RailSection("NO DETOURS STUDIO LLC") {
            Text("Your game. Your device.", style = MaterialTheme.typography.titleMedium)
            Text("No account or network connection is needed. Local results are casual scorecards.", color = TableColors.Muted)
        }
    }
}
