package com.nodetoursstudio.threeball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nodetoursstudio.threeball.ui.ScorekeeperApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ThreeBallApplication
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = GameViewModel(app.games, app.preferences) as T
        }
        setContent { val model: GameViewModel = viewModel(factory = factory); ThreeBallApp(model) }
    }
}

@Composable
internal fun ThreeBallApp(model: GameViewModel) { ScorekeeperApp(model) }
