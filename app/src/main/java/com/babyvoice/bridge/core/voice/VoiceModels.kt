package com.babyvoice.bridge.core.voice

enum class VoiceIntent {
    FULL_BRIEFING,
    LAST_FEEDING,
    LAST_FEEDING_AMOUNT,
    NEXT_FEEDING,
    NEXT_FEEDING_AMOUNT,
    LAST_WAKE,
    CURRENT_SLEEP_STATUS,
    NEXT_SLEEP,
    LAST_DIAPER,
    DATA_FRESHNESS,
    HELP,
    STOP,
    UNKNOWN,
}

sealed interface VoiceState {
    data object Idle : VoiceState

    data object Preparing : VoiceState

    data object Listening : VoiceState

    data object Processing : VoiceState

    data object Speaking : VoiceState

    data object PermissionRequired : VoiceState

    data class Error(val reason: VoiceErrorReason) : VoiceState
}

enum class VoiceErrorReason {
    NO_RECOGNIZER,
    AUDIO_FOCUS_DENIED,
    SPEECH_ERROR,
    NETWORK_CONSENT_REQUIRED,
    UNKNOWN,
}

