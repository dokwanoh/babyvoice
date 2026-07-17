package com.babyvoice.bridge.data.notification

import javax.inject.Inject
import javax.inject.Singleton

data class NotificationPayload(
    val packageName: String,
    val title: String?,
    val text: String?,
)

@Singleton
class BabyTimeNotificationSyncCoordinator @Inject constructor(
    private val diagnosticsRepository: NotificationDiagnosticsRepository,
    private val fieldParser: NotificationFieldParser,
) {
    suspend fun sync(payloads: Iterable<NotificationPayload>) {
        for (payload in payloads) {
            sync(payload)
        }
    }

    suspend fun sync(payload: NotificationPayload) {
        if (!diagnosticsRepository.isTargetAllowed(payload.packageName)) return
        when (val parsed = fieldParser.parse(payload.packageName, payload.title, payload.text)) {
            NotificationParseResult.Unsupported -> Unit
            is NotificationParseResult.Parsed -> diagnosticsRepository.record(parsed)
        }
    }
}
