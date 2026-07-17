package com.babyvoice.bridge.feature.briefing

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babyvoice.bridge.core.common.UserSettings
import com.babyvoice.bridge.core.common.DemoScenario
import com.babyvoice.bridge.core.model.BabyDataProvider
import com.babyvoice.bridge.core.model.BabyDataFailure
import com.babyvoice.bridge.core.model.BabyId
import com.babyvoice.bridge.core.model.BabyProfile
import com.babyvoice.bridge.core.model.SyncStatus
import com.babyvoice.bridge.core.voice.SpeechRecognitionGateway
import com.babyvoice.bridge.core.voice.TtsEngine
import com.babyvoice.bridge.core.voice.VoiceController
import com.babyvoice.bridge.core.voice.VoiceErrorReason
import com.babyvoice.bridge.core.voice.VoiceIntent
import com.babyvoice.bridge.core.voice.VoiceIntentParser
import com.babyvoice.bridge.core.voice.VoiceState
import com.babyvoice.bridge.data.mock.RoomBabyCache
import com.babyvoice.bridge.feature.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BabyVoiceViewModel @Inject constructor(
    private val provider: BabyDataProvider,
    private val settingsRepository: SettingsRepository,
    private val briefingFormatter: BriefingFormatter,
    private val ttsEngine: TtsEngine,
    private val speechGateway: SpeechRecognitionGateway,
    private val voiceIntentParser: VoiceIntentParser,
    private val cache: RoomBabyCache,
    private val clock: Clock,
) : ViewModel() {
    private val voiceController = VoiceController()
    private val statusMessage = MutableStateFlow<String?>(null)
    private val focusIntent = MutableStateFlow<VoiceIntent?>(null)
    private val briefingScript = MutableStateFlow<BriefingScript?>(null)
    private val refreshInProgress = MutableStateFlow(false)
    private val settingsState = settingsRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        UserSettings(),
    )
    private val babiesState = provider.observeBabies().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList(),
    )
    private val selectedBabyState = combine(settingsState, babiesState) { settings, babies ->
        selectBaby(settings, babies)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val snapshotState = selectedBabyState.flatMapLatest { baby ->
        if (baby == null) {
            kotlinx.coroutines.flow.flowOf(null)
        } else {
            provider.observeSnapshot(baby.id)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val voiceState: StateFlow<VoiceState> = voiceController.currentState

    val uiState: StateFlow<BabyVoiceUiState> = combine(
        settingsState,
        babiesState,
        selectedBabyState,
        snapshotState,
        voiceState,
        statusMessage,
        focusIntent,
        briefingScript,
        refreshInProgress,
    ) { values ->
        val settings = values[0] as UserSettings
        val babies = values[1] as List<BabyProfile>
        val selectedBaby = values[2] as BabyProfile?
        val snapshot = values[3] as com.babyvoice.bridge.core.model.BabyCareSnapshot?
        val voice = values[4] as VoiceState
        val message = values[5] as String?
        val intent = values[6] as VoiceIntent?
        val script = values[7] as BriefingScript?
        val refreshing = values[8] as Boolean
        BabyVoiceUiState(
            providerInfo = provider.providerInfo,
            babies = babies,
            selectedBaby = selectedBaby,
            snapshot = snapshot,
            briefing = script,
            settings = settings,
            voiceState = voice,
            focusIntent = intent,
            statusMessage = message,
            isRefreshing = refreshing,
            isCacheEmpty = babies.isEmpty(),
            isExpandedLayout = false,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, BabyVoiceUiState())

    init {
        viewModelScope.launch {
            settingsState.collect { ttsEngine.setSpeechRate(it.speechRate) }
        }
        viewModelScope.launch {
            selectedBabyState.collect { baby ->
                if (baby != null) {
                    refreshForBaby(baby, autoSpeak = false)
                }
            }
        }
    }

    override fun onCleared() {
        ttsEngine.shutdown()
        speechGateway.shutdown()
        super.onCleared()
    }

    fun onDeepLink(uri: Uri) {
        val intent = when (uri.host) {
            "briefing" -> VoiceIntent.FULL_BRIEFING
            "last-feeding" -> VoiceIntent.LAST_FEEDING
            "next-feeding" -> VoiceIntent.NEXT_FEEDING
            "last-wake" -> VoiceIntent.LAST_WAKE
            "next-sleep" -> VoiceIntent.NEXT_SLEEP
            "last-diaper" -> VoiceIntent.LAST_DIAPER
            else -> VoiceIntent.UNKNOWN
        }
        if (intent == VoiceIntent.UNKNOWN) return
        focusIntent.value = intent
        if (intent == VoiceIntent.FULL_BRIEFING) {
            viewModelScope.launch {
                val baby = awaitSelectedBaby() ?: return@launch
                refreshForBaby(baby, autoSpeak = true)
            }
        } else {
            viewModelScope.launch {
                val baby = awaitSelectedBaby() ?: return@launch
                val snapshot = refreshForBaby(baby, autoSpeak = false)
                if (snapshot != null) {
                    speakIntent(intent, snapshot)
                }
            }
        }
    }

    fun onBriefingRequested() {
        focusIntent.value = VoiceIntent.FULL_BRIEFING
        viewModelScope.launch {
            val baby = awaitSelectedBaby() ?: run {
                statusMessage.value = "연결된 육아 기록 공급자가 없어요. 현재는 데모 데이터를 사용할 수 있어요."
                return@launch
            }
            refreshForBaby(baby, autoSpeak = true)
        }
    }

    fun onSpeakRequested(permissionGranted: Boolean) {
        if (!permissionGranted) {
            voiceController.requirePermission()
            statusMessage.value = "마이크 권한이 꺼져 있어요. 화면의 브리핑 듣기 버튼은 사용할 수 있어요."
            return
        }
        if (!speechGateway.isAvailable) {
            voiceController.fail(VoiceErrorReason.NO_RECOGNIZER)
            statusMessage.value = "기기에서 음성 인식기를 찾지 못했어요."
            return
        }
        voiceController.beginListening()
        speechGateway.startListening(
            onTranscript = { transcript ->
                viewModelScope.launch {
                    voiceController.beginProcessing()
                    handleTranscript(transcript)
                }
            },
            onError = { reason ->
                voiceController.fail(reason)
                statusMessage.value = when (reason) {
                    VoiceErrorReason.NO_RECOGNIZER -> "음성 인식기를 사용할 수 없어요."
                    VoiceErrorReason.AUDIO_FOCUS_DENIED -> "오디오 포커스를 얻지 못했어요."
                    VoiceErrorReason.SPEECH_ERROR -> "음성을 이해하지 못했어요."
                    VoiceErrorReason.NETWORK_CONSENT_REQUIRED -> "음성이 네트워크 처리될 수 있어요. 동의가 필요해요."
                    VoiceErrorReason.UNKNOWN -> "음성 입력 중 오류가 생겼어요."
                }
            },
        )
    }

    fun onStopRequested() {
        speechGateway.stopListening()
        ttsEngine.stop()
        voiceController.reset()
    }

    fun onScenarioSelected(scenario: DemoScenario) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(demoScenario = scenario) }
            selectedBabyState.value?.let { refreshForBaby(it, autoSpeak = false) }
        }
    }

    fun onHideSensitiveChanged(checked: Boolean) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(hideSensitiveData = checked) }
        }
    }

    fun onPatternEstimateChanged(checked: Boolean) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(usePatternEstimate = checked) }
        }
    }

    fun onSpeechRateChanged(rate: Float) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(speechRate = rate) }
        }
    }

    fun onBabySelected(babyId: BabyId) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(selectedBabyId = babyId.value) }
        }
    }

    fun onCacheClearRequested() {
        viewModelScope.launch {
            cache.clear()
            statusMessage.value = "로컬 캐시를 삭제했어요."
        }
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    private suspend fun refreshForBaby(
        baby: BabyProfile,
        autoSpeak: Boolean,
    ): com.babyvoice.bridge.core.model.BabyCareSnapshot? {
        refreshInProgress.value = true
        val result = provider.refresh(baby.id)
        refreshInProgress.value = false
        val snapshot = result.getOrNull()
        if (snapshot != null) {
            briefingScript.value = briefingFormatter.format(snapshot, settingsState.value)
            statusMessage.value = snapshot.syncStatus.toStatusMessage()
            if (autoSpeak) {
                speakScript(snapshot)
            }
            return snapshot
        }

        val failure = result.exceptionOrNull()
        statusMessage.value = when (failure) {
            is BabyDataFailure.PermissionDenied -> "권한이 꺼져 있어요."
            is BabyDataFailure.NoData -> "아직 수유 기록이 없어요."
            is BabyDataFailure.NotConfigured -> "연결된 육아 기록 공급자가 없어요. 현재는 데모 데이터를 사용할 수 있어요."
            is BabyDataFailure.Unsupported -> "선택한 파일 형식은 아직 검증되지 않았습니다."
            is BabyDataFailure.StaleData -> "마지막 동기화가 오래되어 최신 정보가 아닐 수 있어요."
            is BabyDataFailure.AuthenticationRequired -> "인증이 필요해요."
            is BabyDataFailure.ParseFailure -> "선택한 파일 형식은 아직 검증되지 않았습니다."
            is BabyDataFailure.Unknown -> "마지막으로 저장된 정보를 읽을게요. 새로고침에는 실패했습니다."
            else -> "마지막으로 저장된 정보를 읽을게요. 새로고침에는 실패했습니다."
        }
        return null
    }

    private suspend fun handleTranscript(transcript: String) {
        val intent = voiceIntentParser.parse(transcript)
        focusIntent.value = intent
        when (intent) {
            VoiceIntent.STOP -> onStopRequested()
            VoiceIntent.HELP -> {
                voiceController.reset()
                statusMessage.value = "원하는 항목을 물어보세요. 예를 들면 마지막 수유, 다음 수면, 기저귀, 또는 브리핑 전체예요."
            }
            VoiceIntent.FULL_BRIEFING -> onBriefingRequested()
            VoiceIntent.UNKNOWN -> {
                voiceController.reset()
                statusMessage.value = "잘 이해하지 못했어요."
            }
            else -> {
                val baby = selectedBabyState.value ?: return
                val snapshot = refreshForBaby(baby, autoSpeak = false) ?: return
                speakIntent(intent, snapshot)
            }
        }
    }

    private suspend fun speakScript(snapshot: com.babyvoice.bridge.core.model.BabyCareSnapshot) {
        val script = briefingScript.value ?: briefingFormatter.format(snapshot, settingsState.value)
        val text = script.utterances.joinToString(" ")
        if (!awaitTtsLanguageSupport()) {
            voiceController.fail(VoiceErrorReason.SPEECH_ERROR)
            statusMessage.value = "TTS 언어를 사용할 수 없어요."
            return
        }
        voiceController.beginSpeaking()
        ttsEngine.speak(text, "briefing") {
            voiceController.reset()
        }
        statusMessage.value = script.followUpPrompt
    }

    private suspend fun speakIntent(
        intent: VoiceIntent,
        snapshot: com.babyvoice.bridge.core.model.BabyCareSnapshot,
    ) {
        val text = briefingFormatter.formatIntent(snapshot, settingsState.value, intent)
        if (text.isBlank()) return
        if (!awaitTtsLanguageSupport()) {
            voiceController.fail(VoiceErrorReason.SPEECH_ERROR)
            statusMessage.value = "TTS 언어를 사용할 수 없어요."
            return
        }
        voiceController.beginSpeaking()
        ttsEngine.speak(text, intent.name) {
            voiceController.reset()
        }
        statusMessage.value = text
    }

    private suspend fun awaitTtsLanguageSupport(): Boolean {
        if (ttsEngine.currentLanguageSupported) return true
        val initialized = withTimeoutOrNull(TTS_READY_TIMEOUT_MILLIS) {
            while (!ttsEngine.isReady && !ttsEngine.currentLanguageSupported) {
                delay(TTS_READY_POLL_MILLIS)
            }
            true
        } ?: false
        return initialized && ttsEngine.currentLanguageSupported
    }

    private suspend fun awaitSelectedBaby(): BabyProfile? {
        selectedBabyState.value?.let { return it }
        return withTimeoutOrNull(SELECTED_BABY_TIMEOUT_MILLIS) {
            selectedBabyState.filterNotNull().first()
        }
    }

    private fun selectBaby(
        settings: UserSettings,
        babies: List<BabyProfile>,
    ): BabyProfile? {
        val selected = settings.selectedBabyId?.let { id -> babies.firstOrNull { it.id.value == id } }
        return selected ?: babies.firstOrNull()
    }

    private fun SyncStatus.toStatusMessage(): String = when (this) {
        SyncStatus.Synced -> "정보를 읽었어요."
        SyncStatus.Syncing -> "정보를 새로 읽는 중이에요."
        is SyncStatus.Stale -> "마지막 동기화가 오래되어 최신 정보가 아닐 수 있어요."
        is SyncStatus.Failed -> when (failure) {
            BabyDataFailure.AuthenticationRequired -> "인증이 필요해요."
            BabyDataFailure.PermissionDenied -> "권한이 꺼져 있어요."
            BabyDataFailure.NoData -> "아직 수유 기록이 없어요."
            BabyDataFailure.StaleData -> "마지막 동기화가 오래되어 최신 정보가 아닐 수 있어요."
            BabyDataFailure.ParseFailure -> "선택한 파일 형식은 아직 검증되지 않았습니다."
            BabyDataFailure.NotConfigured -> "연결된 육아 기록 공급자가 없어요. 현재는 데모 데이터를 사용할 수 있어요."
            BabyDataFailure.Unsupported -> "선택한 파일 형식은 아직 검증되지 않았습니다."
            BabyDataFailure.Unknown -> "마지막으로 저장된 정보를 읽을게요. 새로고침에는 실패했습니다."
        }
        SyncStatus.NotConfigured -> "연결된 육아 기록 공급자가 없어요. 현재는 데모 데이터를 사용할 수 있어요."
        SyncStatus.Unsupported -> "선택한 파일 형식은 아직 검증되지 않았습니다."
    }

    private companion object {
        const val SELECTED_BABY_TIMEOUT_MILLIS = 2_000L
        const val TTS_READY_TIMEOUT_MILLIS = 4_000L
        const val TTS_READY_POLL_MILLIS = 100L
    }
}
