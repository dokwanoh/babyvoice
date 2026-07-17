package com.babyvoice.bridge.core.voice

interface SpeechRecognitionGateway {
    val isAvailable: Boolean
    fun startListening(
        onTranscript: (String) -> Unit,
        onError: (VoiceErrorReason) -> Unit,
    )

    fun stopListening()
    fun shutdown()
}

