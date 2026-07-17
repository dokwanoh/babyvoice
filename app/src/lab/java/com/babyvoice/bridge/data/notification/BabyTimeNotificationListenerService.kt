package com.babyvoice.bridge.data.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

@AndroidEntryPoint
class BabyTimeNotificationListenerService : NotificationListenerService() {
    @Inject lateinit var notificationSyncCoordinator: BabyTimeNotificationSyncCoordinator
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onListenerConnected() {
        super.onListenerConnected()
        scope.launch {
            notificationSyncCoordinator.sync(activeNotifications.orEmpty().map { it.toPayload() })
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        scope.launch {
            notificationSyncCoordinator.sync(sbn.toPayload())
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun StatusBarNotification.toPayload(): NotificationPayload {
        val notification = notification ?: return NotificationPayload(packageName, null, null)
        val extras = notification.extras
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()
        val text = listOfNotNull(
            extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString(),
            extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)?.toString(),
            extras.getCharSequenceArray(android.app.Notification.EXTRA_TEXT_LINES)
                ?.joinToString(separator = "\n") { it.toString() },
            extras.getCharSequence(android.app.Notification.EXTRA_SUB_TEXT)?.toString(),
        ).joinToString(separator = "\n").ifBlank { null }
        return NotificationPayload(
            packageName = packageName,
            title = title,
            text = text,
        )
    }
}
