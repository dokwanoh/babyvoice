package com.babyvoice.bridge.data.notification

import com.babyvoice.bridge.core.model.BabyCareSnapshot
import com.babyvoice.bridge.core.model.BabyId
import com.babyvoice.bridge.core.model.BabyProfile
import com.babyvoice.bridge.core.model.BabyDataFailure
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
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BabyTimeNotificationFieldParser @Inject constructor(
    private val clock: Clock,
) : NotificationFieldParser {
    override fun parse(packageName: String, title: String?, text: String?): NotificationParseResult {
        if (packageName != BABYTIME_PACKAGE) return NotificationParseResult.Unsupported
        val body = listOfNotNull(title, text).joinToString(separator = "\n").trim()
        if (body.isBlank()) return NotificationParseResult.Unsupported

        val now = clock.instant()
        val freshness = Freshness(
            observedAt = now,
            staleAfter = Duration.ofMinutes(30),
            isStale = false,
        )
        val lines = body.lines().map { it.trim() }.filter { it.isNotBlank() }
        val feedingLine = lines.firstOrNull { it.containsAny("수유", "분유", "모유") }
        val sleepLine = lines.firstOrNull { it.containsAny("낮잠", "밤잠", "잠") }
        val wakeLine = lines.firstOrNull { it.containsAny("기상", "일어", "깼") }
        val diaperLine = lines.firstOrNull { it.containsAny("기저귀", "소변", "대변", "쉬", "응가") }

        val lastFeedingAt = feedingLine?.relativePastInstant(now)
        val lastFeeding = lastFeedingAt?.let { observedAt ->
            fieldValue(
                value = FeedingRecord(
                    startedAt = observedAt,
                    endedAt = null,
                    amountMl = feedingLine.amountMl(),
                    feedingType = feedingLine.feedingType(),
                ),
                observedAt = observedAt,
                importedAt = now,
                freshness = freshness,
            )
        }
        val currentSleep = sleepLine
            ?.takeUnless { it.containsAny("기상", "일어", "깼") }
            ?.relativePastInstant(now)
            ?.let { SleepRecord(startedAt = it, endedAt = null, isCurrentlySleeping = true) }
        val lastWakeAt = wakeLine?.relativePastInstant(now)?.let {
            fieldValue(it, observedAt = it, importedAt = now, freshness = freshness)
        }
        val lastDiaper = diaperLine?.relativePastInstant(now)?.let { observedAt ->
            fieldValue(
                value = DiaperRecord(changedAt = observedAt, diaperType = diaperLine.diaperType()),
                observedAt = observedAt,
                importedAt = now,
                freshness = freshness,
            )
        }
        val nextFeedingAt = lines.firstNotNullOfOrNull { line ->
            if (line.contains("다음") && line.containsAny("수유", "분유", "모유")) {
                line.relativeFutureInstant(now)
            } else {
                null
            }
        }?.let {
            fieldValue(it, observedAt = now, importedAt = now, freshness = freshness)
        }

        val babyName = title?.babyNameFromTitle() ?: "BabyTime 알림"
        val babyId = BabyId("babytime-notification:$babyName")
        if (lastFeeding == null && currentSleep == null && lastWakeAt == null && lastDiaper == null && nextFeedingAt == null) {
            if (title?.babyNameFromTitle() == null) {
                return NotificationParseResult.Unsupported
            }
            return NotificationParseResult.Parsed(
                babyId = babyId,
                snapshot = BabyCareSnapshot(
                    baby = BabyProfile(id = babyId, name = babyName, isPrimary = true),
                    currentFeeding = null,
                    lastFeeding = null,
                    nextFeedingTime = null,
                    nextFeedingAmountMl = null,
                    currentSleep = null,
                    lastWakeAt = null,
                    nextSleepAt = null,
                    lastDiaper = null,
                    fetchedAt = now,
                    syncStatus = SyncStatus.Failed(BabyDataFailure.NoData),
                ),
            )
        }

        val snapshot = BabyCareSnapshot(
            baby = BabyProfile(id = babyId, name = babyName, isPrimary = true),
            currentFeeding = null,
            lastFeeding = lastFeeding,
            nextFeedingTime = nextFeedingAt,
            nextFeedingAmountMl = null,
            currentSleep = currentSleep,
            lastWakeAt = lastWakeAt,
            nextSleepAt = null,
            lastDiaper = lastDiaper,
            fetchedAt = now,
            syncStatus = SyncStatus.Synced,
        )
        return NotificationParseResult.Parsed(babyId = babyId, snapshot = snapshot)
    }

    private fun <T> fieldValue(
        value: T,
        observedAt: Instant,
        importedAt: Instant,
        freshness: Freshness,
    ): FieldValue<T> = FieldValue(
        value = value,
        observedAt = observedAt,
        origin = DataOrigin.NOTIFICATION,
        confidence = Confidence.MEDIUM,
        importedAt = importedAt,
        freshness = freshness,
    )

    private fun String.relativePastInstant(now: Instant): Instant? = relativeDuration()?.let { now.minus(it) }

    private fun String.relativeFutureInstant(now: Instant): Instant? = relativeDuration()?.let { now.plus(it) }

    private fun String.relativeDuration(): Duration? {
        val hourMinute = Regex("""(\d+)\s*시간\s*(\d+)\s*분""").find(this)
        if (hourMinute != null) {
            return Duration.ofHours(hourMinute.groupValues[1].toLong())
                .plusMinutes(hourMinute.groupValues[2].toLong())
        }
        val hour = Regex("""(\d+)\s*시간""").find(this)
        if (hour != null) return Duration.ofHours(hour.groupValues[1].toLong())
        val minute = Regex("""(\d+)\s*분""").find(this)
        if (minute != null) return Duration.ofMinutes(minute.groupValues[1].toLong())
        return null
    }

    private fun String.amountMl(): Int? =
        Regex("""(\d{1,4})\s*(?:ml|mL|ML|밀리리터)""").find(this)?.groupValues?.get(1)?.toIntOrNull()

    private fun String.feedingType(): FeedingType = when {
        contains("분유") -> FeedingType.FORMULA
        contains("모유") -> FeedingType.BREAST_MILK
        else -> FeedingType.UNKNOWN
    }

    private fun String.diaperType(): DiaperType = when {
        contains("소변") || contains("쉬") -> DiaperType.URINE
        contains("대변") || contains("응가") -> DiaperType.STOOL
        else -> DiaperType.UNKNOWN
    }

    private fun String.containsAny(vararg tokens: String): Boolean = tokens.any(::contains)

    private fun String.babyNameFromTitle(): String? =
        Regex("""\(([^()]+)\)\s*$""").find(this)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }

    private companion object {
        const val BABYTIME_PACKAGE = "yducky.application.babytime"
    }
}
