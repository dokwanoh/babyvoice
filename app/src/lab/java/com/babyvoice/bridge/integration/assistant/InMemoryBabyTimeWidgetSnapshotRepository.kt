package com.babyvoice.bridge.integration.assistant

import com.babyvoice.bridge.core.model.BabyCareSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Singleton
class InMemoryBabyTimeWidgetSnapshotRepository @Inject constructor() : BabyTimeWidgetSnapshotRepository {
    private val snapshotState = MutableStateFlow<List<BabyCareSnapshot>>(emptyList())

    override val snapshots: Flow<List<BabyCareSnapshot>> = snapshotState

    override suspend fun record(snapshot: BabyCareSnapshot) {
        snapshotState.value = snapshotState.value
            .filterNot { it.baby.id == snapshot.baby.id }
            .plus(snapshot)
    }

    override suspend fun clear() {
        snapshotState.value = emptyList()
    }
}
