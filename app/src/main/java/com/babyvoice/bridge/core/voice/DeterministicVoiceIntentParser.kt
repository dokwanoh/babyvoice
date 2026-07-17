package com.babyvoice.bridge.core.voice

import javax.inject.Inject

class DeterministicVoiceIntentParser @Inject constructor() : VoiceIntentParser {
    override fun parse(input: String): VoiceIntent {
        val normalized = input
            .lowercase()
            .replace(Regex("[\\p{Punct}\\s]+"), " ")
            .trim()

        if (normalized.isBlank()) return VoiceIntent.UNKNOWN

        if (containsAny(normalized, "그만", "멈춰", "종료", "중지", "닫아")) {
            return VoiceIntent.STOP
        }
        if (containsAny(normalized, "도와", "help", "무슨 말", "사용법", "설명")) {
            return VoiceIntent.HELP
        }
        if (containsAny(normalized, "브리핑", "상태 알려줘", "아기 상태", "베이비 타임", "베이비타임", "전체", "한번에", "모두")) {
            return VoiceIntent.FULL_BRIEFING
        }
        if (containsAny(normalized, "정보가 언제", "업데이트", "동기화", "언제 바뀌", "언제 갱신")) {
            return VoiceIntent.DATA_FRESHNESS
        }
        if (containsAny(normalized, "지금 자고", "수면 중", "자고 있어", "잠들어", "깨어 있")) {
            return VoiceIntent.CURRENT_SLEEP_STATUS
        }
        if (containsAny(normalized, "마지막으로 얼마나", "얼마나 먹었", "마지막 수유량", "마지막 분유량")) {
            return VoiceIntent.LAST_FEEDING_AMOUNT
        }
        if (containsAny(normalized, "마지막 수유", "마지막 분유", "마지막 모유", "먹은 것", "먹였어")) {
            return VoiceIntent.LAST_FEEDING
        }
        if (containsAny(normalized, "다음 수유량", "추천량", "얼마나 먹", "얼마 먹", "먹일 양")) {
            return VoiceIntent.NEXT_FEEDING_AMOUNT
        }
        if (containsAny(normalized, "다음 수유", "다음 밥", "다음 분유", "다음 모유", "다음 먹이")) {
            return VoiceIntent.NEXT_FEEDING
        }
        if (containsAny(normalized, "다음 잠", "다음 낮잠", "다음 밤잠", "다음 수면")) {
            return VoiceIntent.NEXT_SLEEP
        }
        if (containsAny(normalized, "언제 일어", "언제 깼", "최근 기상", "최근 일어", "깨어난")) {
            return VoiceIntent.LAST_WAKE
        }
        if (containsAny(normalized, "기저귀", "쉬", "응가", "소변", "대변")) {
            return VoiceIntent.LAST_DIAPER
        }
        return VoiceIntent.UNKNOWN
    }

    private fun containsAny(text: String, vararg tokens: String): Boolean =
        tokens.any { text.contains(it) }
}
