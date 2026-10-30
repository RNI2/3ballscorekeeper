package com.nodetoursstudio.threeball

import android.app.Application
import com.nodetoursstudio.threeball.data.*

class ThreeBallApplication : Application() {
    val database by lazy { GameDatabase.open(this) }
    val games: LocalGameRepository by lazy { RoomGameRepository(database) }
    val preferences: PreferencesRepository by lazy { DataStorePreferencesRepository(this) }
}
