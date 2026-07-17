package com.babyvoice.bridge.core.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class AndroidSpeechRecognitionGateway @Inject constructor(
    @ApplicationContext context: Context,
) : SpeechRecognitionGateway {
    private val appContext = context.applicationContext
    private val recognizer: SpeechRecognizer? = if (SpeechRecognizer.isRecognitionAvailable(appContext)) {
        SpeechRecognizer.createSpeechRecognizer(appContext)
    } else {
        null
    }

    override val isAvailable: Boolean
        get() = recognizer != null

    override fun startListening(
        onTranscript: (String) -> Unit,
        onError: (VoiceErrorReason) -> Unit,
    ) {
        val speechRecognizer = recognizer ?: run {
            onError(VoiceErrorReason.NO_RECOGNIZER)
            return
        }
        speechRecognizer.setRecognitionListener(
            object : RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onError(error: Int) {
                    onError(
                        when (error) {
                            SpeechRecognizer.ERROR_NETWORK,
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> VoiceErrorReason.NETWORK_CONSENT_REQUIRED
                            SpeechRecognizer.ERROR_AUDIO -> VoiceErrorReason.SPEECH_ERROR
                            SpeechRecognizer.ERROR_NO_MATCH,
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceErrorReason.SPEECH_ERROR
                            else -> VoiceErrorReason.UNKNOWN
                        },
                    )
                }

                override fun onResults(results: android.os.Bundle?) {
                    val transcripts = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        .orEmpty()
                    val first = transcripts.firstOrNull().orEmpty()
                    if (first.isBlank()) {
                        onError(VoiceErrorReason.SPEECH_ERROR)
                    } else {
                        onTranscript(first)
                    }
                }

                override fun onPartialResults(partialResults: android.os.Bundle?) = Unit
                override fun onEvent(eventType: Int, params: android.os.Bundle?) = Unit
            },
        )
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.KOREAN.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        speechRecognizer.startListening(intent)
    }

    override fun stopListening() {
        recognizer?.stopListening()
        recognizer?.cancel()
    }

    override fun shutdown() {
        recognizer?.destroy()
    }
}
