package com.babyvoice.bridge.feature.briefing

import com.babyvoice.bridge.core.common.UserSettings
import com.babyvoice.bridge.core.model.BabyCareSnapshot
import com.babyvoice.bridge.core.model.BabyId
import com.babyvoice.bridge.core.model.BabyProfile
import com.babyvoice.bridge.core.model.Confidence
import com.babyvoice.bridge.core.model.DataOrigin
import com.babyvoice.bridge.core.model.DiaperRecord
import com.babyvoice.bridge.core.model.DiaperType
import com.babyvoice.bridge.core.model.FeedingRecord
import com.babyvoice.bridge.core.model.FeedingType
import com.babyvoice.bridge.core.model.FieldValue
import com.babyvoice.bridge.core.model.Freshness
import com.babyvoice.bridge.core.model.SleepRecord
import com.babyvoice.bridge.core.model.SyncStatus
import java.time.Clock
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BriefingFormatterTest {
    private val zone = ZoneId.of("Asia/Seoul")
    private val now = ZonedDateTime.of(2026, 7, 15, 15, 20, 0, 0, zone).toInstant()
    private val formatter = BriefingFormatter(Clock.fixed(now, zone))

    @Test
    fun includesStaleWarningCurrentSleepAndFollowUpPrompt() {
        val snapshot = snapshot(
            currentSleep = SleepRecord(
                startedAt = now.minus(Duration.ofMinutes(68)),
                endedAt = null,
                isCurrentlySleeping = true,
            ),
            nextFeedingAmountMl = null,
            syncStatus = SyncStatus.Stale(Duration.ofHours(2)),
            fetchedAt = now.minus(Duration.ofHours(2)),
        )

        val script = formatter.format(snapshot, UserSettings())

        assertTrue(script.utterances.any { it.contains("마지막 동기화가 2시간 전") })
        assertTrue(script.utterances.any { it.contains("현재 수면 중이며 오후 2시 12분에 잠들었어요.") })
        assertEquals("다음 수유량은 신뢰할 수 있는 정보가 없어 안내하지 않을게요.", script.followUpPrompt)
    }

    @Test
    fun formatsTargetedIntentResponses() {
        val snapshot = snapshot()

        assertEquals(
            "마지막 수유는 오후 12시 10분, 120밀리리터였어요.",
            formatter.formatIntent(snapshot, UserSettings(), com.babyvoice.bridge.core.voice.VoiceIntent.LAST_FEEDING),
        )
        assertEquals(
            "다음 수유량은 120밀리리터예요.",
            formatter.formatIntent(snapshot, UserSettings(), com.babyvoice.bridge.core.voice.VoiceIntent.NEXT_FEEDING_AMOUNT),
        )
        assertEquals(
            "오후 3시 20분에 일어났어요.",
            formatter.formatIntent(snapshot, UserSettings(), com.babyvoice.bridge.core.voice.VoiceIntent.LAST_WAKE),
        )
        assertEquals(
            "마지막 기저귀 교체는 오후 2시 10분이었고 소변이었어요.",
            formatter.formatIntent(snapshot, UserSettings(), com.babyvoice.bridge.core.voice.VoiceIntent.LAST_DIAPER),
        )
    }

    private fun snapshot(
        currentSleep: SleepRecord? = null,
        nextFeedingAmountMl: FieldValue<Int>? = FieldValue(
            value = 120,
            observedAt = now,
            origin = DataOrigin.USER_CONFIGURED,
            confidence = Confidence.HIGH,
            importedAt = now,
            freshness = Freshness(now, Duration.ofHours(2), false),
        ),
        syncStatus: SyncStatus = SyncStatus.Synced,
        fetchedAt: java.time.Instant = now,
    ): BabyCareSnapshot = BabyCareSnapshot(
        baby = BabyProfile(BabyId("haram"), "하람", isPrimary = true),
        currentFeeding = null,
        lastFeeding = FieldValue(
            value = FeedingRecord(
                startedAt = now.minus(Duration.ofHours(3)).minus(Duration.ofMinutes(10)),
                endedAt = now.minus(Duration.ofHours(3)),
                amountMl = 120,
                feedingType = FeedingType.FORMULA,
            ),
            observedAt = now.minus(Duration.ofHours(3)).minus(Duration.ofMinutes(10)),
            origin = DataOrigin.MOCK,
            confidence = Confidence.HIGH,
            importedAt = fetchedAt,
            freshness = Freshness(now, Duration.ofHours(2), false),
        ),
        nextFeedingTime = FieldValue(
            value = now.plus(Duration.ofHours(2)),
            observedAt = now,
            origin = DataOrigin.USER_CONFIGURED,
            confidence = Confidence.HIGH,
            importedAt = fetchedAt,
            freshness = Freshness(now, Duration.ofHours(2), false),
        ),
        nextFeedingAmountMl = nextFeedingAmountMl,
        currentSleep = currentSleep,
        lastWakeAt = FieldValue(
            value = now,
            observedAt = now,
            origin = DataOrigin.MOCK,
            confidence = Confidence.HIGH,
            importedAt = fetchedAt,
            freshness = Freshness(now, Duration.ofHours(2), false),
        ),
        nextSleepAt = FieldValue(
            value = now.plus(Duration.ofMinutes(90)),
            observedAt = now,
            origin = DataOrigin.LOCAL_PATTERN_ESTIMATE,
            confidence = Confidence.MEDIUM,
            importedAt = fetchedAt,
            freshness = Freshness(now, Duration.ofHours(2), false),
        ),
        lastDiaper = FieldValue(
            value = DiaperRecord(
                changedAt = now.minus(Duration.ofHours(1)).minus(Duration.ofMinutes(10)),
                diaperType = DiaperType.URINE,
            ),
            observedAt = now.minus(Duration.ofHours(1)).minus(Duration.ofMinutes(10)),
            origin = DataOrigin.MOCK,
            confidence = Confidence.HIGH,
            importedAt = fetchedAt,
            freshness = Freshness(now, Duration.ofHours(2), false),
        ),
        fetchedAt = fetchedAt,
        syncStatus = syncStatus,
    )
}
