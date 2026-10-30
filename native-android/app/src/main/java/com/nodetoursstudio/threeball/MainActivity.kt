package com.nodetoursstudio.threeball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ThreeBallApp() }
    }
}

private enum class Screen(val title: Int) {
    Setup(R.string.setup),
    Scoring(R.string.scoring),
    History(R.string.history),
    Settings(R.string.settings),
}

@Composable
private fun ThreeBallApp() {
    var selected by rememberSaveable { mutableStateOf(Screen.Setup.name) }
    val screen = Screen.valueOf(selected)
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFFF0D28B), secondary = Color(0xFF39D879),
        background = Color(0xFF07352D), surface = Color(0xFF104C3F),
        onBackground = Color(0xFFFFF8E8), onSurface = Color(0xFFFFF8E8),
    )) {
        Scaffold { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(R.string.studio_name))
                Text(stringResource(R.string.foundation_message), color = MaterialTheme.colorScheme.primary)
                Screen.entries.forEach { destination ->
                    OutlinedButton(onClick = { selected = destination.name }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(destination.title))
                    }
                }
                HorizontalDivider()
                Text(stringResource(screen.title), style = MaterialTheme.typography.headlineMedium)
                when (screen) {
                    Screen.Setup -> GameSetupScreen()
                    Screen.Scoring -> LiveScoringScreen()
                    Screen.History -> GameHistoryScreen()
                    Screen.Settings -> SettingsScreen()
                }
            }
        }
    }
}

// Deliberate screen boundaries for subsequent feature increments.
@Composable
private fun GameSetupScreen() { Text(stringResource(R.string.setup_placeholder)) }
@Composable
private fun LiveScoringScreen() { Text(stringResource(R.string.scoring_placeholder)) }
@Composable
private fun GameHistoryScreen() { Text(stringResource(R.string.history_placeholder)) }
@Composable
private fun SettingsScreen() { Text(stringResource(R.string.settings_placeholder)) }
