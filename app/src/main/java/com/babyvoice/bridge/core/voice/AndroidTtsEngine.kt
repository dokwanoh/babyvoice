package com.babyvoice.bridge.core.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

@Singleton
class AndroidTtsEngine @Inject constructor(
    @ApplicationContext context: Context,
) : TtsEngine {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private val focusRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            .setAcceptsDelayedFocusGain(false)
            .build()
    } else {
        null
    }
    private val readyState = MutableStateFlow(false)
    private val languageSupportedState = MutableStateFlow(false)
    private var speechRate = 1.0f
    private var onDoneCallback: (() -> Unit)? = null
    private var textToSpeech: TextToSpeech? = TextToSpeech(appContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            val engine = textToSpeech ?: return@TextToSpeech
            val locale = Locale.forLanguageTag("ko-KR")
            val languageResult = engine.isLanguageAvailable(locale)
            val supported = languageResult >= TextToSpeech.LANG_AVAILABLE
            languageSupportedState.value = supported
            if (supported) {
                engine.language = locale
                engine.setSpeechRate(speechRate)
            }
            readyState.value = true
        } else {
            readyState.value = false
            languageSupportedState.value = false
        }
    }

    override val isReady: Boolean
        get() = readyState.value

    override val currentLanguageSupported: Boolean
        get() = languageSupportedState.value

    override fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
        textToSpeech?.setSpeechRate(speechRate)
    }

    override fun speak(text: String, utteranceId: String, onDone: (() -> Unit)?) {
        val engine = textToSpeech ?: return
        if (!currentLanguageSupported) return
        onDoneCallback = onDone
        val focusResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(focusRequest ?: return) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
        if (!focusResult) return
        engine.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) = Unit
                override fun onDone(utteranceId: String) {
                    onDoneCallback?.invoke()
                    onDoneCallback = null
                    releaseFocus()
                }

                override fun onError(utteranceId: String) {
                    onDoneCallback = null
                    releaseFocus()
                }
            },
        )
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun stop() {
        onDoneCallback = null
        textToSpeech?.stop()
        releaseFocus()
    }

    override fun shutdown() {
        onDoneCallback = null
        textToSpeech?.shutdown()
        textToSpeech = null
        readyState.value = false
        languageSupportedState.value = false
        releaseFocus()
    }

    private fun releaseFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }
}
