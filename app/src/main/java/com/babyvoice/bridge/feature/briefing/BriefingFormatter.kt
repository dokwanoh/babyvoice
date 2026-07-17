package com.babyvoice.bridge.feature.briefing

import com.babyvoice.bridge.core.common.UserSettings
import com.babyvoice.bridge.core.common.formatAmountMl
import com.babyvoice.bridge.core.common.formatDurationToSentence
import com.babyvoice.bridge.core.common.formatKoreanClockTime
import com.babyvoice.bridge.core.model.BabyCareSnapshot
import com.babyvoice.bridge.core.model.BabyDataFailure
import com.babyvoice.bridge.core.model.FeedingType
import com.babyvoice.bridge.core.model.SyncStatus
import com.babyvoice.bridge.core.voice.VoiceIntent
import javax.inject.Inject
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

data class BriefingScript(
    val utterances: List<String>,
    val followUpPrompt: String?,
)

class BriefingFormatter @Inject constructor(
    private val clock: Clock,
) {
    fun format(snapshot: BabyCareSnapshot, settings: UserSettings): BriefingScript {
        val zoneId = clock.zone
        val now = clock.instant()
        val sentences = mutableListOf<String>()

        when (val status = snapshot.syncStatus) {
            SyncStatus.Synced -> Unit
            SyncStatus.Syncing -> sentences += "정보를 새로 읽는 중이에요."
            is SyncStatus.Stale -> sentences += "마지막 동기화가 오래되어 최신 정보가 아닐 수 있어요."
            is SyncStatus.Failed -> sentences += status.toSafeMessage()
            SyncStatus.NotConfigured -> sentences += "연결된 육아 기록 공급자가 없어요. 현재는 데모 데이터를 사용할 수 있어요."
            SyncStatus.Unsupported -> sentences += "선택한 파일 형식은 아직 검증되지 않았습니다."
        }

        snapshot.lastFeeding?.let {
            val time = it.value.startedAt ?: it.observedAt
            if (time != null) {
                val amount = if (settings.hideSensitiveData) "가려진 정보" else it.value.amountMl?.let(::formatAmountMl) ?: "알 수 없는 양"
                val type = when (it.value.feedingType) {
                    FeedingType.BREAST_MILK -> "모유"
                    FeedingType.FORMULA -> "분유"
                    FeedingType.SOLID -> "이유식"
                    FeedingType.MIXED -> "혼합 수유"
                    FeedingType.UNKNOWN -> "수유"
                }
                sentences += "마지막 ${type}는 ${formatKoreanClockTime(time, zoneId)}, ${amount}였어요."
            }
        }

        snapshot.nextFeedingTime?.let { nextTime ->
            val time = nextTime.value
            val amountSentence = snapshot.nextFeedingAmountMl?.value?.let {
                "다음 수유량은 ${formatAmountMl(it)}예요."
            } ?: "신뢰할 수 있는 추천량 정보는 없어요."
            sentences += "다음 수유 예정은 ${formatKoreanClockTime(time, zoneId)}입니다."
            if (snapshot.nextFeedingAmountMl != null) {
                sentences += amountSentence
            } else {
                sentences += "다음 수유 시각은 ${formatKoreanClockTime(time, zoneId)}으로 예상되지만, 신뢰할 수 있는 추천량 정보는 없어요."
            }
        }

        snapshot.lastWakeAt?.let {
            val time = it.value
            sentences += "${formatKoreanClockTime(time, zoneId)}에 일어났어요."
        }

        snapshot.currentSleep?.let {
            if (it.isCurrentlySleeping) {
                val time = it.startedAt ?: snapshot.lastWakeAt?.value
                if (time != null) {
                    sentences += "현재 수면 중이며 ${formatKoreanClockTime(time, zoneId)}에 잠들었어요."
                } else {
                    sentences += "현재 수면 중이에요."
                }
            }
        }

        snapshot.nextSleepAt?.let {
            sentences += "다음 잠자리는 ${formatKoreanClockTime(it.value, zoneId)}쯤으로 예상돼요."
        }

        snapshot.lastDiaper?.let {
            val time = it.value.changedAt ?: it.observedAt
            if (time != null) {
                val diaperType = when (it.value.diaperType) {
                    com.babyvoice.bridge.core.model.DiaperType.URINE -> "소변"
                    com.babyvoice.bridge.core.model.DiaperType.STOOL -> "대변"
                    com.babyvoice.bridge.core.model.DiaperType.BOTH -> "소변과 대변"
                    com.babyvoice.bridge.core.model.DiaperType.DRY -> "건조"
                    com.babyvoice.bridge.core.model.DiaperType.UNKNOWN -> "기저귀"
                }
                sentences += "마지막 기저귀 교체는 ${formatKoreanClockTime(time, zoneId)}이었고 ${describeDiaperType(diaperType)}."
            }
        }

        if (snapshot.syncStatus is SyncStatus.Stale) {
            val age = Duration.between(snapshot.fetchedAt, now)
            if (age.toMinutes() > 0) {
                sentences += "마지막 동기화가 ${formatDurationToSentence(age)} 전이라 최신 정보가 아닐 수 있어요."
            }
        }

        if (sentences.isEmpty()) {
            sentences += "아직 수유 기록이 없어요."
        }

        val followUp = if (snapshot.nextFeedingAmountMl == null) {
            "다음 수유량은 신뢰할 수 있는 정보가 없어 안내하지 않을게요."
        } else {
            null
        }

        return BriefingScript(
            utterances = sentences.distinct(),
            followUpPrompt = followUp,
        )
    }

    fun formatIntent(
        snapshot: BabyCareSnapshot,
        settings: UserSettings,
        intent: VoiceIntent,
    ): String {
        val zoneId = clock.zone
        return when (intent) {
            VoiceIntent.FULL_BRIEFING -> format(snapshot, settings).utterances.joinToString(" ")
            VoiceIntent.LAST_FEEDING -> snapshot.lastFeeding?.let {
                val time = it.value.startedAt ?: it.observedAt ?: snapshot.fetchedAt
                val amount = it.value.amountMl?.let(::formatAmountMl) ?: "알 수 없는 양"
                "마지막 수유는 ${formatKoreanClockTime(time, zoneId)}, ${amount}였어요."
            } ?: "아직 수유 기록이 없어요."
            VoiceIntent.LAST_FEEDING_AMOUNT -> snapshot.lastFeeding?.value?.amountMl?.let {
                "마지막 수유량은 ${formatAmountMl(it)}였어요."
            } ?: "아직 수유 기록이 없어요."
            VoiceIntent.NEXT_FEEDING -> snapshot.nextFeedingTime?.let {
                "다음 수유 예정은 ${formatKoreanClockTime(it.value, zoneId)}입니다."
            } ?: "다음 수유 예정은 아직 알 수 없어요."
            VoiceIntent.NEXT_FEEDING_AMOUNT -> snapshot.nextFeedingAmountMl?.value?.let {
                "다음 수유량은 ${formatAmountMl(it)}예요."
            } ?: "다음 수유량은 신뢰할 수 있는 정보가 없어 안내하지 않을게요."
            VoiceIntent.LAST_WAKE -> snapshot.lastWakeAt?.let {
                "${formatKoreanClockTime(it.value, zoneId)}에 일어났어요."
            } ?: "최근 기상 시각이 없어요."
            VoiceIntent.CURRENT_SLEEP_STATUS -> snapshot.currentSleep?.let {
                if (it.isCurrentlySleeping) {
                    val startedAt = it.startedAt ?: snapshot.lastWakeAt?.value
                    if (startedAt != null) {
                        "현재 수면 중이며 ${formatKoreanClockTime(startedAt, zoneId)}에 잠들었어요."
                    } else {
                        "현재 수면 중이에요."
                    }
                } else {
                    "지금은 깨어 있어요."
                }
            } ?: "지금은 깨어 있어요."
            VoiceIntent.NEXT_SLEEP -> snapshot.nextSleepAt?.let {
                "다음 잠자리는 ${formatKoreanClockTime(it.value, zoneId)}쯤으로 예상돼요."
            } ?: "다음 잠자리는 아직 알 수 없어요."
            VoiceIntent.LAST_DIAPER -> snapshot.lastDiaper?.let {
                val time = it.value.changedAt ?: it.observedAt ?: snapshot.fetchedAt
                val diaperType = when (it.value.diaperType) {
                    com.babyvoice.bridge.core.model.DiaperType.URINE -> "소변"
                    com.babyvoice.bridge.core.model.DiaperType.STOOL -> "대변"
                    com.babyvoice.bridge.core.model.DiaperType.BOTH -> "소변과 대변"
                    com.babyvoice.bridge.core.model.DiaperType.DRY -> "건조"
                    com.babyvoice.bridge.core.model.DiaperType.UNKNOWN -> "기저귀"
                }
                "마지막 기저귀 교체는 ${formatKoreanClockTime(time, zoneId)}이었고 ${describeDiaperType(diaperType)}."
            } ?: "아직 기저귀 기록이 없어요."
            VoiceIntent.DATA_FRESHNESS -> when (val syncStatus = snapshot.syncStatus) {
                SyncStatus.Synced -> "정보는 최신이에요."
                SyncStatus.Syncing -> "정보를 새로 읽는 중이에요."
                is SyncStatus.Stale -> "마지막 동기화가 오래되어 최신 정보가 아닐 수 있어요."
                is SyncStatus.Failed -> syncStatus.toSafeMessage()
                SyncStatus.NotConfigured -> "연결된 육아 기록 공급자가 없어요. 현재는 데모 데이터를 사용할 수 있어요."
                SyncStatus.Unsupported -> "선택한 파일 형식은 아직 검증되지 않았습니다."
            }
            VoiceIntent.HELP -> "원하는 항목을 물어보세요. 예를 들면 마지막 수유, 다음 수면, 기저귀, 또는 브리핑 전체예요."
            VoiceIntent.STOP -> "그만둘게요."
            VoiceIntent.UNKNOWN -> "잘 이해하지 못했어요."
        }
    }

    private fun SyncStatus.Failed.toSafeMessage(): String = when (failure) {
        BabyDataFailure.AuthenticationRequired -> "인증이 필요해요."
        BabyDataFailure.PermissionDenied -> "권한이 꺼져 있어요."
        BabyDataFailure.NoData -> "아직 수유 기록이 없어요."
        BabyDataFailure.StaleData -> "마지막 동기화가 오래되어 최신 정보가 아닐 수 있어요."
        BabyDataFailure.ParseFailure -> "선택한 파일 형식은 아직 검증되지 않았습니다."
        BabyDataFailure.NotConfigured -> "연결된 육아 기록 공급자가 없어요. 현재는 데모 데이터를 사용할 수 있어요."
        BabyDataFailure.Unsupported -> "선택한 파일 형식은 아직 검증되지 않았습니다."
        BabyDataFailure.Unknown -> "마지막으로 저장된 정보를 읽을게요. 새로고침에는 실패했습니다."
    }

    private fun describeDiaperType(diaperType: String): String = when (diaperType) {
        "소변" -> "소변이었어요"
        "대변" -> "대변이었어요"
        "소변과 대변" -> "소변과 대변이었어요"
        "건조" -> "건조였어요"
        else -> "${diaperType}였어요"
    }
}
