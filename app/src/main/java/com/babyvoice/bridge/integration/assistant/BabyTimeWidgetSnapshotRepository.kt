package com.babyvoice.bridge.integration.assistant

import com.babyvoice.bridge.core.model.BabyCareSnapshot
import kotlinx.coroutines.flow.Flow

interface BabyTimeWidgetSnapshotRepository {
    val snapshots: Flow<List<BabyCareSnapshot>>

    suspend fun record(snapshot: BabyCareSnapshot)

    suspend fun clear()
}
