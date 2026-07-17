package com.babyvoice.bridge.core.common

data class UserSettings(
    val selectedBabyId: String? = null,
    val hideSensitiveData: Boolean = false,
    val speechRate: Float = 1.0f,
    val wakeWindowMinutes: Int = 120,
    val staleAfterMinutes: Int = 120,
    val usePatternEstimate: Boolean = false,
    val demoScenario: DemoScenario = DemoScenario.NORMAL,
    val selectedProviderId: String = "mock",
)
