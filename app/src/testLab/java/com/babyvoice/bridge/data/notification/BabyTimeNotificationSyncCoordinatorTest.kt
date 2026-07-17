package com.babyvoice.bridge.data.notification

import com.babyvoice.bridge.core.model.BabyCareSnapshot
import com.babyvoice.bridge.core.model.BabyId
import com.babyvoice.bridge.core.model.BabyProfile
import com.babyvoice.bridge.core.model.BabyDataFailure
import com.babyvoice.bridge.core.model.Confidence
import com.babyvoice.bridge.core.model.DataOrigin
import com.babyvoice.bridge.core.model.DiaperRecord
import com.babyvoice.bridge.core.model.DiaperType
import com.babyvoice.bridge.core.model.FieldValue
import com.babyvoice.bridge.core.model.Freshness
import com.babyvoice.bridge.core.model.FeedingRecord
import com.babyvoice.bridge.core.model.FeedingType
import com.babyvoice.bridge.core.model.SleepRecord
import com.babyvoice.bridge.core.model.SyncStatus
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BabyTimeNotificationSyncCoordinatorTest {
    @Test
    fun syncProcessesExistingActiveNotifications_whenListenerConnects() = runTest {
        val repository = FakeNotificationDiagnosticsRepository()
        val parser = FakeNotificationFieldParser()
        val coordinator = BabyTimeNotificationSyncCoordinator(repository, parser)

        coordinator.sync(
            listOf(
                NotificationPayload(
                    packageName = "yducky.application.babytime",
                    title = "양호해요 (오서은)",
                    text = "분유 32분 전 120ml",
                ),
                NotificationPayload(
                    packageName = "other.package",
                    title = "ignored",
                    text = "ignored",
                ),
            ),
        )

        assertEquals(listOf(BabyId("babytime-notification:오서은")), repository.recordedBabyIds)
    }

    private class FakeNotificationDiagnosticsRepository : NotificationDiagnosticsRepository {
        private val snapshotState = MutableStateFlow<List<BabyCareSnapshot>>(emptyList())
        val recordedBabyIds = mutableListOf<BabyId>()

        override val targets: Flow<List<NotificationTarget>> =
            flowOf(listOf(NotificationTarget(packageName = "yducky.application.babytime", enabled = true)))

        override val snapshots: Flow<List<BabyCareSnapshot>> = snapshotState

        override fun isTargetAllowed(packageName: String): Boolean = packageName == "yducky.application.babytime"

        override suspend fun record(parsed: NotificationParseResult.Parsed) {
            recordedBabyIds += parsed.babyId
            snapshotState.value = listOf(parsed.snapshot)
        }

        override suspend fun clear() {
            snapshotState.value = emptyList()
        }
    }

    private class FakeNotificationFieldParser : NotificationFieldParser {
        override fun parse(packageName: String, title: String?, text: String?): NotificationParseResult {
            if (packageName != "yducky.application.babytime") return NotificationParseResult.Unsupported
            val babyId = BabyId("babytime-notification:오서은")
            return NotificationParseResult.Parsed(
                babyId = babyId,
                snapshot = BabyCareSnapshot(
                    baby = BabyProfile(id = babyId, name = "오서은", isPrimary = true),
                    currentFeeding = FeedingRecord(
                        startedAt = Instant.parse("2026-07-17T03:28:00Z"),
                        endedAt = null,
                        amountMl = 120,
                        feedingType = FeedingType.FORMULA,
                    ),
                    lastFeeding = FieldValue(
                        value = FeedingRecord(
                            startedAt = Instant.parse("2026-07-17T03:28:00Z"),
                            endedAt = null,
                            amountMl = 120,
                            feedingType = FeedingType.FORMULA,
                        ),
                        observedAt = Instant.parse("2026-07-17T03:28:00Z"),
                        origin = DataOrigin.NOTIFICATION,
                        confidence = Confidence.MEDIUM,
                        importedAt = Instant.parse("2026-07-17T03:30:00Z"),
                        freshness = Freshness(
                            observedAt = Instant.parse("2026-07-17T03:30:00Z"),
                            staleAfter = Duration.ofMinutes(30),
                            isStale = false,
                        ),
                    ),
                    nextFeedingTime = null,
                    nextFeedingAmountMl = null,
                    currentSleep = SleepRecord(
                        startedAt = null,
                        endedAt = null,
                        isCurrentlySleeping = false,
                    ),
                    lastWakeAt = null,
                    nextSleepAt = null,
                    lastDiaper = DiaperRecord(
                        changedAt = null,
                        diaperType = DiaperType.UNKNOWN,
                    ).let {
                        FieldValue(
                            value = it,
                            observedAt = null,
                            origin = DataOrigin.NOTIFICATION,
                            confidence = Confidence.LOW,
                            importedAt = Instant.parse("2026-07-17T03:30:00Z"),
                            freshness = Freshness(
                                observedAt = Instant.parse("2026-07-17T03:30:00Z"),
                                staleAfter = Duration.ofMinutes(30),
                                isStale = false,
                            ),
                        )
                    },
                    fetchedAt = Instant.parse("2026-07-17T03:30:00Z"),
                    syncStatus = SyncStatus.Synced,
                ),
            )
        }
    }
}
