package com.babyvoice.bridge.data.notification

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.flowOf

@Singleton
class NoOpNotificationDiagnosticsRepository @Inject constructor() : NotificationDiagnosticsRepository {
    override val targets = flowOf(emptyList<NotificationTarget>())
    override val snapshots = flowOf(emptyList<com.babyvoice.bridge.core.model.BabyCareSnapshot>())

    override fun isTargetAllowed(packageName: String): Boolean = false

    override suspend fun record(parsed: NotificationParseResult.Parsed) = Unit

    override suspend fun clear() = Unit
}

@Singleton
class UnsupportedNotificationFieldParser @Inject constructor() : NotificationFieldParser {
    override fun parse(packageName: String, title: String?, text: String?): NotificationParseResult =
        NotificationParseResult.Unsupported
}
