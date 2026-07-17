package com.babyvoice.bridge.core.voice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VoiceController {
    private val state = MutableStateFlow<VoiceState>(VoiceState.Idle)

    val currentState: StateFlow<VoiceState> = state.asStateFlow()

    fun prepare() {
        state.value = VoiceState.Preparing
    }

    fun beginListening() {
        state.value = VoiceState.Listening
    }

    fun beginProcessing() {
        state.value = VoiceState.Processing
    }

    fun beginSpeaking() {
        state.value = VoiceState.Speaking
    }

    fun requirePermission() {
        state.value = VoiceState.PermissionRequired
    }

    fun fail(reason: VoiceErrorReason) {
        state.value = VoiceState.Error(reason)
    }

    fun reset() {
        state.value = VoiceState.Idle
    }
}

