package com.babyvoice.bridge.data.mock

import com.babyvoice.bridge.core.model.BabyProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomBabyCache @Inject constructor(
    private val dao: BabyCacheDao,
) {
    fun observeBabies(): Flow<List<BabyProfile>> = dao.observeBabies().map { entities ->
        entities.map { entity ->
            BabyProfile(
                id = com.babyvoice.bridge.core.model.BabyId(entity.babyId),
                name = entity.name,
                isPrimary = entity.isPrimary,
                isTwin = entity.isTwin,
            )
        }
    }

    suspend fun upsertBabies(babies: List<BabyProfile>, updatedAtEpochMillis: Long) {
        dao.upsertAll(
            babies.map {
                CachedBabyEntity(
                    babyId = it.id.value,
                    name = it.name,
                    isPrimary = it.isPrimary,
                    isTwin = it.isTwin,
                    updatedAtEpochMillis = updatedAtEpochMillis,
                )
            },
        )
    }

    suspend fun clear() = dao.clearAll()
}
