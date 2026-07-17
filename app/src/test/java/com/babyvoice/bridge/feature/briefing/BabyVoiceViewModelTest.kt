package com.babyvoice.bridge.feature.briefing

import com.babyvoice.bridge.core.common.UserSettings
import com.babyvoice.bridge.core.model.BabyCareSnapshot
import com.babyvoice.bridge.core.model.BabyDataProvider
import com.babyvoice.bridge.core.model.BabyId
import com.babyvoice.bridge.core.model.BabyProfile
import com.babyvoice.bridge.core.model.ProviderInfo
import com.babyvoice.bridge.core.model.ProviderState
import com.babyvoice.bridge.core.voice.DeterministicVoiceIntentParser
import com.babyvoice.bridge.core.voice.SpeechRecognitionGateway
import com.babyvoice.bridge.core.voice.TtsEngine
import com.babyvoice.bridge.core.voice.VoiceErrorReason
import com.babyvoice.bridge.data.mock.DemoSnapshotGenerator
import com.babyvoice.bridge.data.mock.RoomBabyCache
import com.babyvoice.bridge.feature.settings.SettingsRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class BabyVoiceViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val zone = ZoneId.of("Asia/Seoul")
    private val clock = Clock.fixed(Instant.parse("2026-07-17T12:00:00Z"), zone)

    @Test
    fun briefingRequestWaitsForTtsInitializationBeforeSpeaking() = runTest {
        val tts = FakeTtsEngine()
        val viewModel = createViewModel(tts)

        advanceUntilIdle()
        viewModel.onBriefingRequested()
        advanceTimeBy(500)
        assertTrue("TTS should not speak before initialization", tts.spokenTexts.isEmpty())

        tts.isReadyValue = true
        tts.currentLanguageSupportedValue = true
        advanceTimeBy(1_000)
        advanceUntilIdle()

        assertTrue(
            "Auto briefing should call TTS after the engine becomes ready",
            tts.spokenTexts.any { it.contains("마지막 분유") },
        )
    }

    @Test
    fun briefingRequestWaitsForSelectedBabyBeforeSpeaking() = runTest {
        val tts = FakeTtsEngine().apply {
            isReadyValue = true
            currentLanguageSupportedValue = true
        }
        val snapshot = DemoSnapshotGenerator(clock).generate(
            com.babyvoice.bridge.core.common.DemoScenario.NORMAL,
            UserSettings(),
        ).snapshot
        val provider = FakeBabyDataProvider(snapshot, initialBabies = emptyList())
        val viewModel = createViewModel(tts = tts, provider = provider)

        viewModel.onBriefingRequested()
        advanceTimeBy(500)
        assertTrue("TTS should not speak before a baby is selected", tts.spokenTexts.isEmpty())

        provider.babies.value = listOf(snapshot.baby)
        advanceUntilIdle()

        assertTrue(
            "Auto briefing should speak after the selected baby becomes available",
            tts.spokenTexts.any { it.contains("마지막 분유") },
        )
    }

    private fun createViewModel(
        tts: FakeTtsEngine,
        provider: FakeBabyDataProvider? = null,
    ): BabyVoiceViewModel {
        val settingsRepository = FakeSettingsRepository()
        val snapshot = DemoSnapshotGenerator(clock).generate(
            com.babyvoice.bridge.core.common.DemoScenario.NORMAL,
            UserSettings(),
        ).snapshot
        val cache = mockk<RoomBabyCache>(relaxed = true)
        coEvery { cache.clear() } returns Unit
        return BabyVoiceViewModel(
            provider = provider ?: FakeBabyDataProvider(snapshot),
            settingsRepository = settingsRepository,
            briefingFormatter = BriefingFormatter(clock),
            ttsEngine = tts,
            speechGateway = FakeSpeechRecognitionGateway,
            voiceIntentParser = DeterministicVoiceIntentParser(),
            cache = cache,
            clock = clock,
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeSettingsRepository : SettingsRepository {
    private val state = MutableStateFlow(UserSettings())
    override val settings: Flow<UserSettings> = state

    override suspend fun update(transform: (UserSettings) -> UserSettings) {
        state.value = transform(state.value)
    }
}

private class FakeBabyDataProvider(
    private val snapshot: BabyCareSnapshot,
    initialBabies: List<BabyProfile> = listOf(snapshot.baby),
) : BabyDataProvider {
    val babies = MutableStateFlow(initialBabies)

    override val providerInfo: ProviderInfo = ProviderInfo(
        id = "fake",
        displayName = "테스트",
        flavor = "demo",
        state = ProviderState.READY,
    )

    override fun observeBabies(): Flow<List<BabyProfile>> = babies

    override fun observeSnapshot(babyId: BabyId): Flow<BabyCareSnapshot> =
        MutableStateFlow(snapshot.copy(baby = snapshot.baby.copy(id = babyId)))

    override suspend fun refresh(babyId: BabyId): Result<BabyCareSnapshot> =
        Result.success(snapshot.copy(baby = snapshot.baby.copy(id = babyId)))
}

private class FakeTtsEngine : TtsEngine {
    var isReadyValue: Boolean = false
    var currentLanguageSupportedValue: Boolean = false
    val spokenTexts = mutableListOf<String>()

    override val isReady: Boolean
        get() = isReadyValue

    override val currentLanguageSupported: Boolean
        get() = currentLanguageSupportedValue

    override fun setSpeechRate(rate: Float) = Unit

    override fun speak(text: String, utteranceId: String, onDone: (() -> Unit)?) {
        if (currentLanguageSupportedValue) {
            spokenTexts += text
            onDone?.invoke()
        }
    }

    override fun stop() = Unit

    override fun shutdown() = Unit
}

private object FakeSpeechRecognitionGateway : SpeechRecognitionGateway {
    override val isAvailable: Boolean = true

    override fun startListening(
        onTranscript: (String) -> Unit,
        onError: (VoiceErrorReason) -> Unit,
    ) = Unit

    override fun stopListening() = Unit

    override fun shutdown() = Unit
}
