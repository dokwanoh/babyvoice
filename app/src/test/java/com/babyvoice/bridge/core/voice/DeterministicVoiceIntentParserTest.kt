package com.babyvoice.bridge.core.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class DeterministicVoiceIntentParserTest {
    private val parser = DeterministicVoiceIntentParser()

    @Test
    fun parsesCommonKoreanVoiceCommands() {
        val cases = listOf(
            "아기 브리핑" to VoiceIntent.FULL_BRIEFING,
            "아기 상태 알려줘" to VoiceIntent.FULL_BRIEFING,
            "베이비 타임" to VoiceIntent.FULL_BRIEFING,
            "베이비타임" to VoiceIntent.FULL_BRIEFING,
            "마지막 수유 언제였어" to VoiceIntent.LAST_FEEDING,
            "마지막으로 얼마나 먹었어" to VoiceIntent.LAST_FEEDING_AMOUNT,
            "마지막 분유량 알려줘" to VoiceIntent.LAST_FEEDING_AMOUNT,
            "다음 수유 언제야" to VoiceIntent.NEXT_FEEDING,
            "다음 밥 언제" to VoiceIntent.NEXT_FEEDING,
            "다음 수유량 알려줘" to VoiceIntent.NEXT_FEEDING_AMOUNT,
            "먹일 양 알려줘" to VoiceIntent.NEXT_FEEDING_AMOUNT,
            "언제 일어났어" to VoiceIntent.LAST_WAKE,
            "최근 기상 시각 알려줘" to VoiceIntent.LAST_WAKE,
            "지금 자고 있어?" to VoiceIntent.CURRENT_SLEEP_STATUS,
            "수면 중이야?" to VoiceIntent.CURRENT_SLEEP_STATUS,
            "다음 잠은 언제야" to VoiceIntent.NEXT_SLEEP,
            "다음 낮잠 알려줘" to VoiceIntent.NEXT_SLEEP,
            "기저귀 언제 갈았지" to VoiceIntent.LAST_DIAPER,
            "응가 언제 했어" to VoiceIntent.LAST_DIAPER,
            "정보가 언제 업데이트됐어" to VoiceIntent.DATA_FRESHNESS,
            "도와줘" to VoiceIntent.HELP,
            "그만" to VoiceIntent.STOP,
            "브리핑 전체" to VoiceIntent.FULL_BRIEFING,
            "먹였어" to VoiceIntent.LAST_FEEDING,
        )

        cases.forEach { (input, expected) ->
            assertEquals(input, expected, parser.parse(input))
        }
    }
}
