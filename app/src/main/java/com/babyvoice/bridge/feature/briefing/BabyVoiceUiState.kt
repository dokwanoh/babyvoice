package com.babyvoice.bridge.feature.briefing

import com.babyvoice.bridge.core.common.UserSettings
import com.babyvoice.bridge.core.model.BabyCareSnapshot
import com.babyvoice.bridge.core.model.BabyProfile
import com.babyvoice.bridge.core.model.ProviderInfo
import com.babyvoice.bridge.core.model.ProviderState
import com.babyvoice.bridge.core.voice.VoiceIntent
import com.babyvoice.bridge.core.voice.VoiceState

data class BabyVoiceUiState(
    val providerInfo: ProviderInfo = ProviderInfo(
        id = "mock",
        displayName = "데모 데이터",
        flavor = "demo",
        state = ProviderState.READY,
    ),
    val babies: List<BabyProfile> = emptyList(),
    val selectedBaby: BabyProfile? = null,
    val snapshot: BabyCareSnapshot? = null,
    val briefing: BriefingScript? = null,
    val settings: UserSettings = UserSettings(),
    val voiceState: VoiceState = VoiceState.Idle,
    val focusIntent: VoiceIntent? = null,
    val statusMessage: String? = null,
    val isRefreshing: Boolean = false,
    val isCacheEmpty: Boolean = true,
    val isExpandedLayout: Boolean = false,
)

