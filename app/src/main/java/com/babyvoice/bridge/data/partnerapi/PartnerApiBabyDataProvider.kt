package com.babyvoice.bridge.data.partnerapi

import com.babyvoice.bridge.core.model.BabyCareSnapshot
import com.babyvoice.bridge.core.model.BabyDataFailure
import com.babyvoice.bridge.core.model.BabyDataProvider
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
import com.babyvoice.bridge.core.model.ProviderInfo
import com.babyvoice.bridge.core.model.ProviderState
import com.babyvoice.bridge.core.model.RecommendationOrigin
import com.babyvoice.bridge.core.model.SleepRecord
import com.babyvoice.bridge.core.model.SyncStatus
import com.babyvoice.bridge.feature.settings.SettingsRepository
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Singleton
class PartnerApiBabyDataProvider @Inject constructor(
    private val authStore: PartnerApiAuthStore,
    private val client: PartnerApiClient,
    private val mapper: PartnerApiMapper,
    private val clock: Clock,
    private val settingsRepository: SettingsRepository,
) : BabyDataProvider {
    override val providerInfo: ProviderInfo
        get() = ProviderInfo(
            id = "partner-api",
            displayName = "공식 연동",
            flavor = "production",
            state = if (authStore.accessToken.isNullOrBlank()) ProviderState.NOT_CONFIGURED else ProviderState.READY,
        )

    override fun observeBabies(): Flow<List<BabyProfile>> = flowOf(emptyList())

    override fun observeSnapshot(babyId: BabyId): Flow<BabyCareSnapshot> = settingsRepository.settings.map {
        placeholderSnapshot(babyId, it.staleAfterMinutes.toLong())
    }

    override suspend fun refresh(babyId: BabyId): Result<BabyCareSnapshot> {
        if (authStore.accessToken.isNullOrBlank()) {
            return Result.failure(BabyDataFailure.NotConfigured)
        }
        return Result.failure(BabyDataFailure.NotConfigured)
    }

    private fun placeholderSnapshot(babyId: BabyId, staleAfterMinutes: Long): BabyCareSnapshot {
        val now = clock.instant()
        val baby = BabyProfile(id = babyId, name = "아기", isPrimary = true)
        val freshness = Freshness(
            observedAt = now,
            staleAfter = Duration.ofMinutes(staleAfterMinutes),
            isStale = false,
        )
        return BabyCareSnapshot(
            baby = baby,
            currentFeeding = null,
            lastFeeding = null,
            nextFeedingTime = FieldValue(
                value = now.plus(Duration.ofHours(3)),
                observedAt = now,
                origin = DataOrigin.PARTNER_API,
                confidence = Confidence.UNKNOWN,
                importedAt = now,
                freshness = freshness,
            ),
            nextFeedingAmountMl = null,
            currentSleep = null,
            lastWakeAt = null,
            nextSleepAt = null,
            lastDiaper = null,
            fetchedAt = now,
            syncStatus = SyncStatus.NotConfigured,
        )
    }
}

