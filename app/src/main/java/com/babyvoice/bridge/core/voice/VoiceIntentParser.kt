package com.babyvoice.bridge.core.voice

interface VoiceIntentParser {
    fun parse(input: String): VoiceIntent
}

