package com.babyvoice.bridge.data.notification

import com.babyvoice.bridge.core.model.BabyCareSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Singleton
class InMemoryNotificationDiagnosticsRepository @Inject constructor() : NotificationDiagnosticsRepository {
    private val targetPackage = "yducky.application.babytime"
    private val snapshotState = MutableStateFlow<List<BabyCareSnapshot>>(emptyList())

    override val targets: Flow<List<NotificationTarget>> = MutableStateFlow(
        listOf(NotificationTarget(packageName = targetPackage, enabled = true)),
    )
    override val snapshots: Flow<List<BabyCareSnapshot>> = snapshotState

    override fun isTargetAllowed(packageName: String): Boolean = packageName == targetPackage

    override suspend fun record(parsed: NotificationParseResult.Parsed) {
        snapshotState.value = snapshotState.value
            .filterNot { it.baby.id == parsed.babyId }
            .plus(parsed.snapshot)
    }

    override suspend fun clear() {
        snapshotState.value = emptyList()
    }
}
