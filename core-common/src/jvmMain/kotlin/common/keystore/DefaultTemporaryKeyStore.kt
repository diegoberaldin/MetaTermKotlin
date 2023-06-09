package common.keystore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import common.files.FileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

class DefaultTemporaryKeyStore(
    private val fileManager: FileManager,
) : TemporaryKeyStore {

    companion object {
        private const val FILE_NAME = "main.preferences_pb"
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private val dataStore: DataStore<Preferences> = createDataStore()

    private fun createDataStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            corruptionHandler = null,
            migrations = emptyList(),
            scope = scope,
            produceFile = {
                val path = fileManager.getFilePath(FILE_NAME)
                File(path)
            },
        )

    override suspend fun save(key: String, value: Boolean) {
        dataStore.edit {
            it[booleanPreferencesKey(key)] = value
        }
    }

    override suspend fun get(key: String, default: Boolean) = withTimeoutOrNull(500) {
        dataStore.data.map {
            it[booleanPreferencesKey(key)]
        }.first()
    } ?: default

    override suspend fun save(key: String, value: String) {
        dataStore.edit {
            it[stringPreferencesKey(key)] = value
        }
    }

    override suspend fun get(key: String, default: String): String = withTimeoutOrNull(500) {
        dataStore.data.map {
            it[stringPreferencesKey(key)]
        }.first()
    } ?: default

    override suspend fun save(key: String, value: Int) {
        dataStore.edit {
            it[intPreferencesKey(key)] = value
        }
    }

    override suspend fun get(key: String, default: Int): Int = withTimeoutOrNull(500) {
        dataStore.data.map {
            it[intPreferencesKey(key)]
        }.first()
    } ?: default

    override suspend fun save(key: String, value: Float) {
        dataStore.edit {
            it[floatPreferencesKey(key)] = value
        }
    }

    override suspend fun get(key: String, default: Float): Float = withTimeoutOrNull(500) {
        dataStore.data.map {
            it[floatPreferencesKey(key)]
        }.first()
    } ?: default

    override suspend fun save(key: String, value: Double) {
        dataStore.edit {
            it[doublePreferencesKey(key)] = value
        }
    }

    override suspend fun get(key: String, default: Double): Double = withTimeoutOrNull(500) {
        dataStore.data.map {
            it[doublePreferencesKey(key)]
        }.first()
    } ?: default
}
