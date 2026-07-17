package com.babyvoice.bridge.core.model

import java.time.Duration
import java.time.Instant

@JvmInline
value class BabyId(val value: String)

data class BabyProfile(
    val id: BabyId,
    val name: String,
    val isPrimary: Boolean = false,
    val isTwin: Boolean = false,
)

data class FeedingRecord(
    val startedAt: Instant?,
    val endedAt: Instant?,
    val amountMl: Int?,
    val feedingType: FeedingType,
)

data class SleepRecord(
    val startedAt: Instant?,
    val endedAt: Instant?,
    val isCurrentlySleeping: Boolean,
)

data class DiaperRecord(
    val changedAt: Instant?,
    val diaperType: DiaperType,
)

data class FeedingPlan(
    val nextFeedingAt: Instant?,
    val targetAmountMl: Int?,
    val origin: RecommendationOrigin,
    val generatedAt: Instant?,
    val sampleCount: Int,
    val confidence: Confidence,
    val explanation: String,
)

data class SleepPlan(
    val nextSleepAt: Instant?,
    val origin: RecommendationOrigin,
    val generatedAt: Instant?,
    val sampleCount: Int,
    val confidence: Confidence,
    val explanation: String,
)

data class Freshness(
    val observedAt: Instant?,
    val staleAfter: Duration,
    val isStale: Boolean,
)

data class FieldValue<T>(
    val value: T,
    val observedAt: Instant?,
    val origin: DataOrigin,
    val confidence: Confidence,
    val importedAt: Instant?,
    val freshness: Freshness,
)

data class BabyCareSnapshot(
    val baby: BabyProfile,
    val currentFeeding: FeedingRecord?,
    val lastFeeding: FieldValue<FeedingRecord>?,
    val nextFeedingTime: FieldValue<Instant>?,
    val nextFeedingAmountMl: FieldValue<Int>?,
    val currentSleep: SleepRecord?,
    val lastWakeAt: FieldValue<Instant>?,
    val nextSleepAt: FieldValue<Instant>?,
    val lastDiaper: FieldValue<DiaperRecord>?,
    val fetchedAt: Instant,
    val syncStatus: SyncStatus,
)

enum class FeedingType {
    BREAST_MILK,
    FORMULA,
    SOLID,
    MIXED,
    UNKNOWN,
}

enum class DiaperType {
    URINE,
    STOOL,
    BOTH,
    DRY,
    UNKNOWN,
}

enum class Confidence {
    HIGH,
    MEDIUM,
    LOW,
    UNKNOWN,
}

enum class DataOrigin {
    MOCK,
    USER_EXPORT,
    NOTIFICATION,
    ACCESSIBILITY_WIDGET,
    PARTNER_API,
    USER_CONFIGURED,
    LOCAL_PATTERN_ESTIMATE,
}

enum class RecommendationOrigin {
    AUTHORITATIVE_PROVIDER,
    USER_CONFIGURED,
    PATTERN_ESTIMATE,
    NONE,
}

sealed interface SyncStatus {
    data object Synced : SyncStatus

    data object Syncing : SyncStatus

    data class Stale(val age: Duration) : SyncStatus

    data class Failed(val failure: BabyDataFailure) : SyncStatus

    data object NotConfigured : SyncStatus

    data object Unsupported : SyncStatus
}
