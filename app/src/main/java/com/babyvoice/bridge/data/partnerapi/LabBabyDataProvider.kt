package com.babyvoice.bridge.data.partnerapi

import android.content.Context
import com.babyvoice.bridge.core.model.BabyCareSnapshot
import com.babyvoice.bridge.core.model.BabyDataFailure
import com.babyvoice.bridge.core.model.BabyDataProvider
import com.babyvoice.bridge.core.model.BabyId
import com.babyvoice.bridge.core.model.BabyProfile
import com.babyvoice.bridge.core.model.ProviderInfo
import com.babyvoice.bridge.core.model.ProviderState
import com.babyvoice.bridge.core.model.SyncStatus
import com.babyvoice.bridge.integration.assistant.BabyTimeWidgetSnapshotRepository
import com.babyvoice.bridge.integration.assistant.isAccessibilityServiceEnabled
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class LabBabyDataProvider @Inject constructor(
    private val clock: Clock,
    @ApplicationContext private val context: Context,
    private val widgetSnapshotRepository: BabyTimeWidgetSnapshotRepository,
) : BabyDataProvider {
    override val providerInfo: ProviderInfo
        get() = ProviderInfo(
            id = "babytime-widget",
            displayName = "BabyTime 위젯",
            flavor = "lab",
            state = currentState(),
        )

    override fun observeBabies(): Flow<List<BabyProfile>> = widgetSnapshotRepository.snapshots.map { snapshots ->
        snapshots.map { it.baby }.distinctBy { it.id }
    }

    override fun observeSnapshot(babyId: BabyId): Flow<BabyCareSnapshot> =
        widgetSnapshotRepository.snapshots.map { snapshots ->
            snapshots.firstOrNull { it.baby.id == babyId } ?: emptySnapshot(babyId)
        }

    override suspend fun refresh(babyId: BabyId): Result<BabyCareSnapshot> {
        val snapshot = widgetSnapshotRepository.snapshots.first().firstOrNull { it.baby.id == babyId }
        return when {
            snapshot != null -> {
                Result.success(snapshot)
            }
            hasAccessibilityAccess() -> {
                Result.failure(BabyDataFailure.NoData)
            }
            else -> {
                Result.failure(BabyDataFailure.PermissionDenied)
            }
        }
    }

    private fun emptySnapshot(babyId: BabyId): BabyCareSnapshot = BabyCareSnapshot(
        baby = BabyProfile(babyId, "BabyTime 위젯", isPrimary = true),
        currentFeeding = null,
        lastFeeding = null,
        nextFeedingTime = null,
        nextFeedingAmountMl = null,
        currentSleep = null,
        lastWakeAt = null,
        nextSleepAt = null,
        lastDiaper = null,
        fetchedAt = clock.instant(),
        syncStatus = if (hasAccessibilityAccess()) {
            SyncStatus.Failed(BabyDataFailure.NoData)
        } else {
            SyncStatus.Failed(BabyDataFailure.PermissionDenied)
        },
    )

    private fun currentState(): ProviderState = if (hasAccessibilityAccess()) {
        ProviderState.READY
    } else {
        ProviderState.NEEDS_PERMISSION
    }

    private fun hasAccessibilityAccess(): Boolean =
        context.isAccessibilityServiceEnabled("com.babyvoice.bridge.integration.assistant.BabyTimeWidgetAccessibilityService")
}
