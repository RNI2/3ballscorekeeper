package com.nodetoursstudio.threeball

import android.app.Application
import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.test.core.app.ApplicationProvider
import com.nodetoursstudio.threeball.data.DataStorePreferencesRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class PreferencesTest {
    private fun file(name: String): File = File(ApplicationProvider.getApplicationContext<Context>().cacheDir, "$name.preferences_pb").also { it.delete() }
    @Test fun preferencesPersistAcrossStoreRecreation() = runBlocking {
        val file = file("restored")
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val repo = DataStorePreferencesRepository(PreferenceDataStoreFactory.create(scope = scope, produceFile = { file }))
        assertTrue(repo.preferences.first().keepScreenAwake)
        repo.setKeepScreenAwake(false)
        scope.cancel(); scope.coroutineContext[Job]!!.join()
        val nextScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        try {
            val restored = DataStorePreferencesRepository(PreferenceDataStoreFactory.create(scope = nextScope, produceFile = { file }))
            assertFalse(restored.preferences.first().keepScreenAwake)
        } finally { nextScope.cancel(); nextScope.coroutineContext[Job]!!.join(); file.delete() }
    }
    @Test fun corruptedPreferencesRecoverToSafeDefaults() = runBlocking {
        val file = file("corrupted"); file.writeText("invalid protobuf")
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        try {
            val store = PreferenceDataStoreFactory.create(scope = scope,
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }, produceFile = { file })
            val repo = DataStorePreferencesRepository(store)
            assertTrue(repo.preferences.first().keepScreenAwake)
            repo.setKeepScreenAwake(false)
            assertFalse(repo.preferences.first().keepScreenAwake)
        } finally { scope.cancel(); scope.coroutineContext[Job]!!.join(); file.delete() }
    }
}
