package com.babyvoice.bridge.feature.settings

import com.babyvoice.bridge.core.common.UserSettings
import com.babyvoice.bridge.core.common.DemoScenario
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun update(transform: (UserSettings) -> UserSettings)
}
