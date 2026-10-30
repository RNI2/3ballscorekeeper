package com.nodetoursstudio.threeball

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nodetoursstudio.threeball.data.*
import com.nodetoursstudio.threeball.engine.*
import com.nodetoursstudio.threeball.records.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class GameUiState(
    val busy: Boolean = true,
    val loaded: Boolean = false,
    val active: GameRecord? = null,
    val game: GameState? = null,
    val history: List<GameRecord> = emptyList(),
    val recoveredRecords: Int = 0,
    val preferences: GamePreferences = GamePreferences(),
    val error: String? = null,
)

class GameViewModel(
    private val games: LocalGameRepository,
    private val preferences: PreferencesRepository,
    private val clock: () -> Long = System::currentTimeMillis,
    private val worker: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val mutable = MutableStateFlow(GameUiState())
    val ui: StateFlow<GameUiState> = mutable.asStateFlow()
    private val mutex = Mutex()
    init {
        reload()
        viewModelScope.launch {
            preferences.preferences.catch { mutable.update { it.copy(error = "Could not read preferences. Default settings are in use.") } }
                .collect { value -> mutable.update { it.copy(preferences = value) } }
        }
    }
    private fun operation(block: suspend () -> Unit) {
        viewModelScope.launch {
            mutex.withLock {
                mutable.update { it.copy(busy = true, error = null) }
                try { block() }
                catch (e: CancellationException) { throw e }
                catch (_: Exception) { mutable.update { it.copy(error = "Could not complete the action. Your last saved game is preserved. Retry or reload.") } }
                finally { mutable.update { it.copy(busy = false) } }
            }
        }
    }
    fun reload() = operation {
        val saved = withContext(worker) { games.load() }
        val state = withContext(worker) { saved.active?.let { Records.state(it) } }
        mutable.update { it.copy(loaded = true, active = saved.active, game = state,
            history = saved.history, recoveredRecords = saved.recoveredRecords) }
    }
    fun start(config: GameConfig, names: List<String>) = operation {
        check(Features.localScoring && mutable.value.loaded && mutable.value.active == null)
        val record = withContext(worker) { Records.start(config, names, clock()) }
        games.save(record, null)
        mutable.update { it.copy(active = record, game = Records.state(record)) }
    }
    fun command(action: Action) = operation {
        val old = requireNotNull(mutable.value.active)
        val next = withContext(worker) { Records.apply(old, action, clock()) }
        if (action.type == "FINISH") {
            games.finish(next, old)
            mutable.update { it.copy(active = null, game = null,
                history = if (next.status == "COMPLETED") listOf(next) + it.history else it.history) }
        } else {
            games.save(next, old)
            val state = withContext(worker) { Records.state(next) }
            mutable.update { it.copy(active = next, game = state) }
        }
    }
    fun abandon() = operation {
        games.abandon(requireNotNull(mutable.value.active))
        mutable.update { it.copy(active = null, game = null) }
    }
    fun setKeepScreenAwake(value: Boolean) = operation { preferences.setKeepScreenAwake(value) }
    fun dismissError() { mutable.update { it.copy(error = null) } }
}
