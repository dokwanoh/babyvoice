package com.babyvoice.bridge.feature.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.babyvoice.bridge.core.common.DemoScenario
import com.babyvoice.bridge.core.common.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext context: Context,
) : SettingsRepository {
    private val dataStore = context.applicationContext.dataStore

    override val settings: Flow<UserSettings> = dataStore.data.map { preferences ->
        UserSettings(
            selectedBabyId = preferences[Keys.selectedBabyId],
            hideSensitiveData = preferences[Keys.hideSensitiveData] ?: false,
            speechRate = preferences[Keys.speechRate] ?: 1.0f,
            wakeWindowMinutes = preferences[Keys.wakeWindowMinutes] ?: 120,
            staleAfterMinutes = preferences[Keys.staleAfterMinutes] ?: 120,
            usePatternEstimate = preferences[Keys.usePatternEstimate] ?: false,
            demoScenario = runCatching {
                DemoScenario.valueOf(preferences[Keys.demoScenario] ?: DemoScenario.NORMAL.name)
            }.getOrDefault(DemoScenario.NORMAL),
            selectedProviderId = preferences[Keys.selectedProviderId] ?: "mock",
        )
    }

    override suspend fun update(transform: (UserSettings) -> UserSettings) {
        dataStore.edit { preferences ->
            val current = UserSettings(
                selectedBabyId = preferences[Keys.selectedBabyId],
                hideSensitiveData = preferences[Keys.hideSensitiveData] ?: false,
                speechRate = preferences[Keys.speechRate] ?: 1.0f,
                wakeWindowMinutes = preferences[Keys.wakeWindowMinutes] ?: 120,
                staleAfterMinutes = preferences[Keys.staleAfterMinutes] ?: 120,
                usePatternEstimate = preferences[Keys.usePatternEstimate] ?: false,
                demoScenario = runCatching {
                    DemoScenario.valueOf(preferences[Keys.demoScenario] ?: DemoScenario.NORMAL.name)
                }.getOrDefault(DemoScenario.NORMAL),
                selectedProviderId = preferences[Keys.selectedProviderId] ?: "mock",
            )
            val updated = transform(current)
            if (updated.selectedBabyId == null) {
                preferences.remove(Keys.selectedBabyId)
            } else {
                preferences[Keys.selectedBabyId] = updated.selectedBabyId
            }
            preferences[Keys.hideSensitiveData] = updated.hideSensitiveData
            preferences[Keys.speechRate] = updated.speechRate
            preferences[Keys.wakeWindowMinutes] = updated.wakeWindowMinutes
            preferences[Keys.staleAfterMinutes] = updated.staleAfterMinutes
            preferences[Keys.usePatternEstimate] = updated.usePatternEstimate
            preferences[Keys.demoScenario] = updated.demoScenario.name
            preferences[Keys.selectedProviderId] = updated.selectedProviderId
        }
    }

    private object Keys {
        val selectedBabyId = stringPreferencesKey("selectedBabyId")
        val hideSensitiveData = booleanPreferencesKey("hideSensitiveData")
        val speechRate = floatPreferencesKey("speechRate")
        val wakeWindowMinutes = intPreferencesKey("wakeWindowMinutes")
        val staleAfterMinutes = intPreferencesKey("staleAfterMinutes")
        val usePatternEstimate = booleanPreferencesKey("usePatternEstimate")
        val demoScenario = stringPreferencesKey("demoScenario")
        val selectedProviderId = stringPreferencesKey("selectedProviderId")
    }
}
