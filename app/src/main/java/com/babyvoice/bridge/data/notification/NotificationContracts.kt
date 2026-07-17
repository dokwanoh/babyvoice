package com.babyvoice.bridge.data.notification

import com.babyvoice.bridge.core.model.BabyCareSnapshot
import com.babyvoice.bridge.core.model.BabyId
import kotlinx.coroutines.flow.Flow

data class NotificationTarget(
    val packageName: String,
    val enabled: Boolean,
)

interface NotificationDiagnosticsRepository {
    val targets: Flow<List<NotificationTarget>>
    val snapshots: Flow<List<BabyCareSnapshot>>
    fun isTargetAllowed(packageName: String): Boolean
    suspend fun record(parsed: NotificationParseResult.Parsed)
    suspend fun clear()
}

interface NotificationFieldParser {
    fun parse(packageName: String, title: String?, text: String?): NotificationParseResult
}

sealed interface NotificationParseResult {
    data object Unsupported : NotificationParseResult

    data class Parsed(
        val babyId: BabyId,
        val snapshot: BabyCareSnapshot,
    ) : NotificationParseResult
}
