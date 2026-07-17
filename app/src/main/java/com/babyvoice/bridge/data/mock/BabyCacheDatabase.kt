package com.babyvoice.bridge.data.mock

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CachedBabyEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class BabyCacheDatabase : RoomDatabase() {
    abstract fun babyCacheDao(): BabyCacheDao
}

