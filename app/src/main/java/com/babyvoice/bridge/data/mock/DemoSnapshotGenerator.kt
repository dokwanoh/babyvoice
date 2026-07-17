package com.babyvoice.bridge.data.mock

import com.babyvoice.bridge.core.common.DemoScenario
import com.babyvoice.bridge.core.common.UserSettings
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

data class DemoSnapshotBundle(
    val babies: List<BabyProfile>,
    val snapshot: BabyCareSnapshot,
)

class DemoSnapshotGenerator(
    private val clock: Clock,
) {
    fun generate(
        scenario: DemoScenario,
        settings: UserSettings,
    ): DemoSnapshotBundle {
        val now = clock.instant()
        val babyId = BabyId(settings.selectedBabyId ?: "haram")
        val babies = when (scenario) {
            DemoScenario.MULTIPLE_BABIES -> listOf(
                BabyProfile(BabyId("oseoun"), "오서은", isPrimary = true),
                BabyProfile(BabyId("seojun"), "서준", isTwin = true),
            )
            else -> listOf(BabyProfile(babyId, "오서은", isPrimary = true))
        }
        val selectedBaby = babies.firstOrNull { it.id == babyId } ?: babies.first()

        val stale = scenario == DemoScenario.STALE_DATA
        val fetchedAt = if (stale) now.minus(Duration.ofHours(2)) else now
        val freshness = Freshness(
            observedAt = now.minus(Duration.ofMinutes(20)),
            staleAfter = Duration.ofMinutes(settings.staleAfterMinutes.toLong()),
            isStale = stale,
        )

        val noRecords = scenario == DemoScenario.NO_RECORDS
        val providerError = scenario == DemoScenario.PROVIDER_ERROR
        val permissionDenied = scenario == DemoScenario.PERMISSION_DENIED
        val syncing = scenario == DemoScenario.SYNCING
        val currentFeeding = scenario == DemoScenario.CURRENT_FEEDING
        val currentSleep = scenario == DemoScenario.CURRENT_SLEEP
        val noNextAmount = scenario == DemoScenario.NO_NEXT_FEEDING_AMOUNT

        val lastFeeding = if (noRecords) null else FieldValue(
            value = FeedingRecord(
                startedAt = now.minus(Duration.ofMinutes(32)),
                endedAt = now.minus(Duration.ofMinutes(24)),
                amountMl = 630,
                feedingType = FeedingType.FORMULA,
            ),
            observedAt = now.minus(Duration.ofMinutes(32)),
            origin = DataOrigin.MOCK,
            confidence = Confidence.HIGH,
            importedAt = fetchedAt,
            freshness = freshness,
        )

        val lastWakeAt = if (noRecords) null else FieldValue(
            value = now.minus(Duration.ofMinutes(6)),
            observedAt = now.minus(Duration.ofMinutes(6)),
            origin = DataOrigin.MOCK,
            confidence = Confidence.HIGH,
            importedAt = fetchedAt,
            freshness = freshness,
        )

        val nextFeedingAt = if (noRecords) null else FieldValue(
            value = now.plus(Duration.ofHours(2)).plus(Duration.ofMinutes(31)),
            observedAt = now,
            origin = if (settings.usePatternEstimate) DataOrigin.LOCAL_PATTERN_ESTIMATE else DataOrigin.USER_CONFIGURED,
            confidence = if (settings.usePatternEstimate) Confidence.MEDIUM else Confidence.HIGH,
            importedAt = fetchedAt,
            freshness = freshness,
        )

        val nextFeedingAmount = if (noRecords || noNextAmount) null else FieldValue(
            value = 120,
            observedAt = now,
            origin = DataOrigin.USER_CONFIGURED,
            confidence = Confidence.HIGH,
            importedAt = fetchedAt,
            freshness = freshness,
        )

        val sleepStartedAt = now.minus(Duration.ofMinutes(48))
        val currentSleepRecord = if (currentSleep) {
            SleepRecord(
                startedAt = sleepStartedAt,
                endedAt = null,
                isCurrentlySleeping = true,
            )
        } else null

        val nextSleepAt = if (noRecords) null else FieldValue(
            value = now.plus(Duration.ofMinutes(33)),
            observedAt = now,
            origin = if (settings.usePatternEstimate) DataOrigin.LOCAL_PATTERN_ESTIMATE else DataOrigin.USER_CONFIGURED,
            confidence = if (settings.usePatternEstimate) Confidence.MEDIUM else Confidence.HIGH,
            importedAt = fetchedAt,
            freshness = freshness,
        )

        val lastDiaper = if (noRecords) null else FieldValue(
            value = DiaperRecord(
                changedAt = now.minus(Duration.ofHours(1)).minus(Duration.ofMinutes(29)),
                diaperType = DiaperType.URINE,
            ),
            observedAt = now.minus(Duration.ofHours(1)).minus(Duration.ofMinutes(29)),
            origin = DataOrigin.MOCK,
            confidence = Confidence.HIGH,
            importedAt = fetchedAt,
            freshness = freshness,
        )

        val syncStatus = when {
            providerError -> SyncStatus.Failed(BabyDataFailure.Unknown)
            permissionDenied -> SyncStatus.Failed(BabyDataFailure.PermissionDenied)
            syncing -> SyncStatus.Syncing
            stale -> SyncStatus.Stale(Duration.ofHours(2))
            noRecords -> SyncStatus.Failed(BabyDataFailure.NoData)
            else -> SyncStatus.Synced
        }

        val snapshot = BabyCareSnapshot(
            baby = selectedBaby,
            currentFeeding = if (currentFeeding) {
                FeedingRecord(
                    startedAt = now.minus(Duration.ofMinutes(15)),
                    endedAt = null,
                    amountMl = 60,
                    feedingType = FeedingType.FORMULA,
                )
            } else null,
            lastFeeding = lastFeeding,
            nextFeedingTime = nextFeedingAt,
            nextFeedingAmountMl = nextFeedingAmount,
            currentSleep = currentSleepRecord,
            lastWakeAt = lastWakeAt,
            nextSleepAt = nextSleepAt,
            lastDiaper = lastDiaper,
            fetchedAt = fetchedAt,
            syncStatus = syncStatus,
        )

        return DemoSnapshotBundle(
            babies = babies,
            snapshot = snapshot,
        )
    }
}
