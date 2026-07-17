package com.babyvoice.bridge.data.mock

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_babies")
data class CachedBabyEntity(
    @PrimaryKey val babyId: String,
    val name: String,
    val isPrimary: Boolean,
    val isTwin: Boolean,
    val updatedAtEpochMillis: Long,
)

