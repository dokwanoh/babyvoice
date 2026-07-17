package com.babyvoice.bridge.data.partnerapi

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpPartnerApiAuthStore @Inject constructor() : PartnerApiAuthStore {
    override val accessToken: String? = null

    override suspend fun saveAccessToken(token: String) = Unit

    override suspend fun clear() = Unit
}

@Singleton
class NotConfiguredPartnerApiClient @Inject constructor() : PartnerApiClient {
    override suspend fun fetchBabies(): Result<List<PartnerBabyDto>> =
        Result.failure(IllegalStateException("partner_api_not_configured"))

    override suspend fun fetchSnapshot(babyId: com.babyvoice.bridge.core.model.BabyId): Result<PartnerSnapshotDto> =
        Result.failure(IllegalStateException("partner_api_not_configured"))

    override suspend fun fetchActivities(
        babyId: com.babyvoice.bridge.core.model.BabyId,
        cursor: String?,
    ): Result<PartnerActivitiesDto> = Result.failure(IllegalStateException("partner_api_not_configured"))
}

