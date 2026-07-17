package com.babyvoice.bridge.data.mock

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BabyCacheDao {
    @Query("SELECT * FROM cached_babies ORDER BY isPrimary DESC, name ASC")
    fun observeBabies(): Flow<List<CachedBabyEntity>>

    @Upsert
    suspend fun upsertAll(babies: List<CachedBabyEntity>)

    @Query("DELETE FROM cached_babies")
    suspend fun clearAll()
}

