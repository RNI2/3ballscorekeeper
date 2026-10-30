package com.nodetoursstudio.threeball.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*
import java.io.IOException

private val Context.gamePreferences by preferencesDataStore(name = "game-preferences",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() })
data class GamePreferences(val keepScreenAwake: Boolean = true)
interface PreferencesRepository {
    val preferences: Flow<GamePreferences>
    suspend fun setKeepScreenAwake(value: Boolean)
}
class DataStorePreferencesRepository(private val store: DataStore<Preferences>) : PreferencesRepository {
    constructor(context: Context) : this(context.applicationContext.gamePreferences)
    private val awake = booleanPreferencesKey("keep_screen_awake")
    override val preferences = store.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { GamePreferences(it[awake] ?: true) }
    override suspend fun setKeepScreenAwake(value: Boolean) { store.edit { it[awake] = value } }
}
