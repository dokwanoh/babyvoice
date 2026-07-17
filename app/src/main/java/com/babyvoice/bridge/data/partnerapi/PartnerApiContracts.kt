package com.babyvoice.bridge.data.partnerapi

import com.babyvoice.bridge.core.model.BabyId
import com.babyvoice.bridge.core.model.BabyProfile
import com.babyvoice.bridge.core.model.BabyCareSnapshot
import kotlinx.serialization.Serializable

interface PartnerApiAuthStore {
    val accessToken: String?
    suspend fun saveAccessToken(token: String)
    suspend fun clear()
}

interface PartnerApiClient {
    suspend fun fetchBabies(): Result<List<PartnerBabyDto>>
    suspend fun fetchSnapshot(babyId: BabyId): Result<PartnerSnapshotDto>
    suspend fun fetchActivities(babyId: BabyId, cursor: String?): Result<PartnerActivitiesDto>
}

@Serializable
data class PartnerActivitiesDto(
    val activities: List<PartnerActivityDto>,
    val nextCursor: String?,
)

@Serializable
data class PartnerBabyDto(
    val id: String,
    val name: String,
    val isPrimary: Boolean,
    val isTwin: Boolean,
)

data class PartnerSnapshotDto(
    val baby: PartnerBabyDto,
    val snapshot: BabyCareSnapshot,
)

@Serializable
data class PartnerActivityDto(
    val babyId: String,
    val kind: String,
    val observedAtMillis: Long,
)

data class PartnerApiStatus(
    val configured: Boolean,
    val message: String,
)
