package com.babyvoice.bridge.di

import com.babyvoice.bridge.data.notification.BabyTimeNotificationFieldParser
import com.babyvoice.bridge.data.notification.InMemoryNotificationDiagnosticsRepository
import com.babyvoice.bridge.data.notification.NotificationDiagnosticsRepository
import com.babyvoice.bridge.data.notification.NotificationFieldParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds
    abstract fun bindNotificationDiagnosticsRepository(
        repository: InMemoryNotificationDiagnosticsRepository,
    ): NotificationDiagnosticsRepository

    @Binds
    abstract fun bindNotificationFieldParser(
        parser: BabyTimeNotificationFieldParser,
    ): NotificationFieldParser
}
