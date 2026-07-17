package com.babyvoice.bridge.integration.assistant

import com.babyvoice.bridge.core.model.BabyCareSnapshot
import com.babyvoice.bridge.core.model.BabyDataFailure
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
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BabyTimeWidgetSnapshotParser @Inject constructor(
    private val clock: Clock,
) {
    fun parse(rawWindowText: CharSequence?): Result<BabyCareSnapshot> {
        val fields = WidgetFieldIndex(
            rawWindowText
            ?.lineSequence()
            ?.mapNotNull(::parseField)
            ?.toList()
            .orEmpty(),
        )

        val babyName = fields.valueForSuffix("tvBabyName")
            ?: if (fields.looksLikeBabyTimeWidget()) {
                fields.firstMeaningfulText()
            } else {
                null
            }
            ?: return Result.failure(BabyDataFailure.Unsupported)

        val now = clock.instant()
        val freshness = Freshness(
            observedAt = now,
            staleAfter = Duration.ofMinutes(120),
            isStale = false,
        )
        val baby = BabyProfile(
            id = BabyId("widget:${babyName.sanitizeBabyId()}"),
            name = babyName,
            isPrimary = true,
        )
        val lastFeeding = parseLastFeeding(fields, now, freshness)
        val nextFeedingTime = parseNextFeedingTime(fields, now, freshness)
        val nextFeedingAmountMl = parseNextFeedingAmount(fields, now, freshness)
        val currentSleep = parseCurrentSleep(fields, now)
        val lastWakeAt = parseLastWake(fields, now, freshness)
        val nextSleepAt = parseNextSleep(fields, now, freshness)
        val lastDiaper = parseLastDiaper(fields, now, freshness)

        val snapshot = BabyCareSnapshot(
            baby = baby,
            currentFeeding = null,
            lastFeeding = lastFeeding,
            nextFeedingTime = nextFeedingTime,
            nextFeedingAmountMl = nextFeedingAmountMl,
            currentSleep = currentSleep,
            lastWakeAt = lastWakeAt,
            nextSleepAt = nextSleepAt,
            lastDiaper = lastDiaper,
            fetchedAt = now,
            syncStatus = if (listOf(lastFeeding, nextFeedingTime, nextFeedingAmountMl, currentSleep, lastWakeAt, nextSleepAt, lastDiaper).any { it != null }) {
                SyncStatus.Synced
            } else {
                SyncStatus.Failed(BabyDataFailure.NoData)
            },
        )
        return Result.success(snapshot)
    }

    private fun parseLastFeeding(
        fields: WidgetFieldIndex,
        now: Instant,
        freshness: Freshness,
    ): FieldValue<FeedingRecord>? {
        val title = fields.valueForSuffix("tv1stTitle")
            ?: fields.valueForSuffix("tvTitle")
        val delay = fields.valueForSuffix("tv1stDelay")
            ?: fields.valueForSuffix("tvType")
        if (title.isNullOrBlank() || delay.isNullOrBlank()) return null
        val duration = delay.toRelativeDuration() ?: return null
        val amount = fields.valueForSuffix("tv2ndDelay")
            ?.toAmountMl()
        return fieldValue(
            value = FeedingRecord(
                startedAt = now.minus(duration),
                endedAt = null,
                amountMl = amount,
                feedingType = title.toFeedingType(),
            ),
            observedAt = now.minus(duration),
            freshness = freshness,
        )
    }

    private fun parseNextFeedingTime(
        fields: WidgetFieldIndex,
        now: Instant,
        freshness: Freshness,
    ): FieldValue<Instant>? {
        val prefix = fields.valueForSuffix("tv1st_extra_info_prefix")
            ?: fields.valueForSuffix("tvTypeInfo")
        val delay = fields.valueForSuffix("tv1st_extra_info")
            ?: fields.valueForSuffix("tvType")
        if (prefix.isNullOrBlank() || delay.isNullOrBlank()) return null
        if (!prefix.contains("다음") || !prefix.contains("수유")) return null
        val duration = delay.toRelativeDuration() ?: return null
        val observedAt = now.plus(duration)
        return fieldValue(
            value = observedAt,
            observedAt = now,
            freshness = freshness,
        )
    }

    private fun parseNextFeedingAmount(
        fields: WidgetFieldIndex,
        now: Instant,
        freshness: Freshness,
    ): FieldValue<Int>? {
        val explicitAmount = fields.valueForSuffix("tvNextFeedingAmount")
            ?.toAmountMl()
        if (explicitAmount != null) {
            return fieldValue(
                value = explicitAmount,
                observedAt = now,
                freshness = freshness,
            )
        }
        return null
    }

    private fun parseCurrentSleep(
        fields: WidgetFieldIndex,
        now: Instant,
    ): SleepRecord? {
        val label = fields.valueForSuffix("tvSleepStatus")
            ?: fields.valueForSuffix("tvCurrentSleep")
            ?: return null
        if (!label.containsAny("수면 중", "자는 중", "잠드는 중", "잠들었어요")) return null
        val startedAt = fields.valueForSuffix("tvSleepStatusDuration")
            ?.toRelativeDuration()
            ?.let(now::minus)
        return SleepRecord(
            startedAt = startedAt,
            endedAt = null,
            isCurrentlySleeping = true,
        )
    }

    private fun parseLastWake(
        fields: WidgetFieldIndex,
        now: Instant,
        freshness: Freshness,
    ): FieldValue<Instant>? {
        val label = fields.valueForSuffix("tvLastWake")
            ?: fields.valueForSuffix("tvWake")
            ?: return null
        val duration = label.toRelativeDuration() ?: return null
        val observedAt = now.minus(duration)
        return fieldValue(
            value = observedAt,
            observedAt = observedAt,
            freshness = freshness,
        )
    }

    private fun parseNextSleep(
        fields: WidgetFieldIndex,
        now: Instant,
        freshness: Freshness,
    ): FieldValue<Instant>? {
        val label = fields.valueForSuffix("tvNextSleep")
            ?: fields.valueForSuffix("tvSleepPlan")
            ?: return null
        val duration = label.toRelativeDuration() ?: return null
        val observedAt = now.plus(duration)
        return fieldValue(
            value = observedAt,
            observedAt = now,
            freshness = freshness,
        )
    }

    private fun parseLastDiaper(
        fields: WidgetFieldIndex,
        now: Instant,
        freshness: Freshness,
    ): FieldValue<DiaperRecord>? {
        val title = fields.valueForSuffix("tv4thTitle")
            ?: fields.valueForSuffix("tvDiaper")
            ?: return null
        val delay = fields.valueForSuffix("tv4thDelay")
            ?: fields.valueForSuffix("tvDiaperDelay")
            ?: return null
        val duration = delay.toRelativeDuration() ?: return null
        val observedAt = now.minus(duration)
        return fieldValue(
            value = DiaperRecord(
                changedAt = observedAt,
                diaperType = title.toDiaperType(),
            ),
            observedAt = observedAt,
            freshness = freshness,
        )
    }

    private fun parseField(line: String): WidgetField? {
        val trimmed = line.trim()
        if (!trimmed.startsWith("id=")) return null
        val idEnd = trimmed.indexOf(' ')
        if (idEnd <= 3) return null
        val id = trimmed.substring(3, idEnd).trim()
        val textPrefix = "text="
        val textStart = trimmed.indexOf(textPrefix)
        if (textStart < 0) return null
        val text = trimmed.substring(textStart + textPrefix.length).trim()
        return WidgetField(id = id, text = text)
    }

    private fun WidgetFieldIndex.firstMeaningfulText(): String? =
        fields.firstNotNullOfOrNull { field ->
            field.text.takeIf { value ->
                value.isNotBlank() &&
                    !value.contains("ㆍ") &&
                    !value.contains("분유") &&
                    !value.contains("모유") &&
                    !value.contains("밤잠") &&
                    !value.contains("낮잠") &&
                    !value.contains("소변") &&
                    !value.contains("대변") &&
                    !value.contains("기저귀") &&
                    !value.contains("수면") &&
                    !value.contains("ml", ignoreCase = true) &&
                    !value.contains("시간") &&
                    !value.contains("전")
            }
        }

    private fun WidgetFieldIndex.looksLikeBabyTimeWidget(): Boolean =
        fields.any { field ->
            field.id.endsWith("/tv1stTitle") ||
                field.id.endsWith("/tv1stDelay") ||
                field.id.endsWith("/tv1st_extra_info") ||
                field.id.endsWith("/tv2ndTitle") ||
                field.id.endsWith("/tv3rdTitle") ||
                field.id.endsWith("/tv4thTitle") ||
                field.id.endsWith("/tvTotalAmountOfToday") ||
                field.id.endsWith("/tvSleepStatus") ||
                field.id.endsWith("/tvLastWake") ||
                field.id.endsWith("/tvNextSleep") ||
                field.id.endsWith("/tvDiaper")
        }

    private fun WidgetFieldIndex.valueForSuffix(suffix: String): String? =
        fields.firstOrNull { it.id.endsWith("/$suffix") || it.id == suffix }?.text?.trim()?.takeIf { it.isNotBlank() }

    private fun <T> fieldValue(
        value: T,
        observedAt: Instant,
        freshness: Freshness,
    ): FieldValue<T> = FieldValue(
        value = value,
        observedAt = observedAt,
        origin = DataOrigin.ACCESSIBILITY_WIDGET,
        confidence = Confidence.HIGH,
        importedAt = now(),
        freshness = freshness,
    )

    private fun now(): Instant = clock.instant()

    private fun String.toRelativeDuration(): Duration? {
        val normalized = replace("전", "")
            .replace("남음", "")
            .replace("쯤", "")
            .replace("예상", "")
            .trim()
        val hours = Regex("""(\d+)\s*시간""").find(normalized)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
        val minutes = Regex("""(\d+)\s*분""").find(normalized)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
        if (!normalized.contains("시간") && !normalized.contains("분")) return null
        return Duration.ofHours(hours).plusMinutes(minutes)
    }

    private fun String.toAmountMl(): Int? =
        Regex("""(\d{1,4})\s*(?:ml|밀리리터)""", RegexOption.IGNORE_CASE).find(this)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: trim().toIntOrNull()

    private fun String.toFeedingType(): FeedingType = when {
        contains("모유") -> FeedingType.BREAST_MILK
        contains("분유") -> FeedingType.FORMULA
        contains("이유식") -> FeedingType.SOLID
        contains("혼합") -> FeedingType.MIXED
        else -> FeedingType.UNKNOWN
    }

    private fun String.toDiaperType(): DiaperType = when {
        contains("소변") || contains("쉬") -> DiaperType.URINE
        contains("대변") || contains("응가") -> DiaperType.STOOL
        contains("건조") -> DiaperType.DRY
        else -> DiaperType.UNKNOWN
    }

    private fun String.containsAny(vararg needles: String): Boolean = needles.any(::contains)

    private fun String.sanitizeBabyId(): String = lowercase()
        .replace(Regex("""[^a-z0-9가-힣]+"""), "-")
        .trim('-')
        .ifBlank { "baby" }

    private data class WidgetField(
        val id: String,
        val text: String,
    )

    private data class WidgetFieldIndex(
        val fields: List<WidgetField>,
    )
}
