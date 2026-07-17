package com.babyvoice.bridge.feature.briefing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.babyvoice.bridge.core.common.DemoScenario
import com.babyvoice.bridge.core.common.UserSettings
import com.babyvoice.bridge.core.model.ProviderInfo
import com.babyvoice.bridge.core.model.ProviderState
import com.babyvoice.bridge.core.voice.VoiceState
import com.babyvoice.bridge.data.mock.DemoSnapshotGenerator
import com.babyvoice.bridge.core.ui.BabyVoiceTheme
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BabyVoiceScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun launch_renders_demo_controls_and_provider_state() {
        composeRule.setScreenContent()

        composeRule.onNodeWithText("브리핑 듣기").assertIsDisplayed()
        composeRule.onNodeWithText("말하기").assertIsDisplayed()
        composeRule.onNodeWithText("그만").assertIsDisplayed()
        composeRule.onNodeWithText("공급자 연결됨").assertIsDisplayed()
        composeRule.onNodeWithText("오서은").assertIsDisplayed()
        composeRule.onNodeWithText("마지막 분유", substring = true).assertIsDisplayed()
    }

    @Test
    fun scenario_selection_rebuilds_the_briefing() {
        composeRule.setScreenContent()

        composeRule.onNodeWithText("기록 없음").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("기록 없음").assertIsSelected()
        composeRule.onNodeWithText("아직 수유 기록이 없어요.").assertIsDisplayed()
    }

    @Test
    fun hide_sensitive_toggle_masks_amounts_in_the_ui() {
        composeRule.setScreenContent(initialScenario = DemoScenario.NO_NEXT_FEEDING_AMOUNT)

        composeRule.onNodeWithText("630밀리리터", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("민감정보 가리기").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("가려진 정보", substring = true).assertIsDisplayed()
        composeRule.onAllNodesWithText("630밀리리터", substring = true).assertCountEquals(0)
    }

    @Test
    fun cache_clear_button_invokes_the_callback() {
        var cleared = false

        composeRule.setScreenContent(
            onCacheClearRequested = {
                cleared = true
            },
        )

        composeRule.onNodeWithText("캐시 삭제").performClick()
        composeRule.waitForIdle()

        assertTrue(cleared)
    }

    @Test
    fun no_next_amount_shows_the_follow_up_prompt() {
        composeRule.setScreenContent(initialScenario = DemoScenario.NO_NEXT_FEEDING_AMOUNT)

        composeRule.onNodeWithText("다음 수유량은 신뢰할 수 있는 정보가 없어 안내하지 않을게요.").assertIsDisplayed()
        composeRule.onNodeWithText("후속 질문").assertIsDisplayed()
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setScreenContent(
        initialScenario: DemoScenario = DemoScenario.NORMAL,
        initialSettings: UserSettings = UserSettings(),
        onCacheClearRequested: () -> Unit = {},
    ) {
        val testClock = Clock.fixed(Instant.parse("2026-07-16T05:10:00Z"), ZoneId.of("Asia/Seoul"))
        val generator = DemoSnapshotGenerator(testClock)
        val formatter = BriefingFormatter(testClock)

        setContent {
            var currentScenario by remember { mutableStateOf(initialScenario) }
            var currentSettings by remember { mutableStateOf(initialSettings) }
            val currentState = remember(currentScenario, currentSettings) {
                val bundle = generator.generate(currentScenario, currentSettings)
                val briefing = formatter.format(bundle.snapshot, currentSettings)
                BabyVoiceUiState(
                    providerInfo = ProviderInfo(
                        id = "mock",
                        displayName = "데모 데이터",
                        flavor = "demo",
                        state = ProviderState.READY,
                    ),
                    babies = bundle.babies,
                    selectedBaby = bundle.snapshot.baby,
                    snapshot = bundle.snapshot,
                    briefing = briefing,
                    settings = currentSettings,
                    voiceState = VoiceState.Idle,
                    isCacheEmpty = bundle.babies.isEmpty(),
                )
            }

            BabyVoiceTheme {
                BabyVoiceScreen(
                    state = currentState,
                    onBriefingRequested = {},
                    onSpeakRequested = {},
                    onStopRequested = {},
                    onScenarioSelected = { scenario ->
                        currentScenario = scenario
                    },
                    onHideSensitiveChanged = { checked ->
                        currentSettings = currentSettings.copy(hideSensitiveData = checked)
                    },
                    onPatternEstimateChanged = { checked ->
                        currentSettings = currentSettings.copy(usePatternEstimate = checked)
                    },
                    onSpeechRateChanged = { rate ->
                        currentSettings = currentSettings.copy(speechRate = rate)
                    },
                    onBabySelected = { babyId ->
                        currentSettings = currentSettings.copy(selectedBabyId = babyId.value)
                    },
                    onCacheClearRequested = onCacheClearRequested,
                    onAccessibilitySettingsRequested = {},
                    onStatusDismissed = {},
                )
            }
        }
    }
}
