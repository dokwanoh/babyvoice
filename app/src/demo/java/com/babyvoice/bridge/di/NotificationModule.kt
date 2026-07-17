package com.babyvoice.bridge.di

import com.babyvoice.bridge.data.notification.NoOpNotificationDiagnosticsRepository
import com.babyvoice.bridge.data.notification.NotificationDiagnosticsRepository
import com.babyvoice.bridge.data.notification.NotificationFieldParser
import com.babyvoice.bridge.data.notification.UnsupportedNotificationFieldParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds
    abstract fun bindNotificationDiagnosticsRepository(
        repository: NoOpNotificationDiagnosticsRepository,
    ): NotificationDiagnosticsRepository

    @Binds
    abstract fun bindNotificationFieldParser(
        parser: UnsupportedNotificationFieldParser,
    ): NotificationFieldParser
}
