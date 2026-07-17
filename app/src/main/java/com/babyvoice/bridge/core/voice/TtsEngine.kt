package com.babyvoice.bridge.core.voice

interface TtsEngine {
    val isReady: Boolean
    val currentLanguageSupported: Boolean
    fun setSpeechRate(rate: Float)
    fun speak(text: String, utteranceId: String, onDone: (() -> Unit)? = null)
    fun stop()
    fun shutdown()
}
