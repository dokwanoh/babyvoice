package com.babyvoice.bridge.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import com.babyvoice.bridge.BuildConfig
import com.babyvoice.bridge.core.voice.AndroidSpeechRecognitionGateway
import com.babyvoice.bridge.core.voice.AndroidTtsEngine
import com.babyvoice.bridge.core.voice.DeterministicVoiceIntentParser
import com.babyvoice.bridge.core.voice.SpeechRecognitionGateway
import com.babyvoice.bridge.core.voice.TtsEngine
import com.babyvoice.bridge.core.voice.VoiceIntentParser
import com.babyvoice.bridge.data.mock.BabyCacheDao
import com.babyvoice.bridge.data.mock.BabyCacheDatabase
import com.babyvoice.bridge.data.mock.MockBabyDataProvider
import com.babyvoice.bridge.data.export.DefaultExportFormatDetector
import com.babyvoice.bridge.data.export.ExportFormatDetector
import com.babyvoice.bridge.data.export.ExportRecordParser
import com.babyvoice.bridge.data.export.SafeZipReader
import com.babyvoice.bridge.data.export.UnsupportedExportRecordParser
import com.babyvoice.bridge.data.partnerapi.PartnerApiBabyDataProvider
import com.babyvoice.bridge.data.partnerapi.PartnerApiAuthStore
import com.babyvoice.bridge.data.partnerapi.PartnerApiClient
import com.babyvoice.bridge.data.partnerapi.NoOpPartnerApiAuthStore
import com.babyvoice.bridge.data.partnerapi.NotConfiguredPartnerApiClient
import com.babyvoice.bridge.data.partnerapi.LabBabyDataProvider
import com.babyvoice.bridge.core.model.BabyDataProvider
import com.babyvoice.bridge.feature.settings.DataStoreSettingsRepository
import com.babyvoice.bridge.feature.settings.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun bindSettingsRepository(
        repository: DataStoreSettingsRepository,
    ): SettingsRepository

    @Binds
    abstract fun bindTtsEngine(
        engine: AndroidTtsEngine,
    ): TtsEngine

    @Binds
    abstract fun bindSpeechGateway(
        gateway: AndroidSpeechRecognitionGateway,
    ): SpeechRecognitionGateway

    @Binds
    abstract fun bindVoiceIntentParser(
        parser: DeterministicVoiceIntentParser,
    ): VoiceIntentParser

    @Binds
    abstract fun bindPartnerApiAuthStore(
        store: NoOpPartnerApiAuthStore,
    ): PartnerApiAuthStore

    @Binds
    abstract fun bindPartnerApiClient(
        client: NotConfiguredPartnerApiClient,
    ): PartnerApiClient

    @Binds
    abstract fun bindExportFormatDetector(
        detector: DefaultExportFormatDetector,
    ): ExportFormatDetector

    @Binds
    @IntoSet
    abstract fun bindExportRecordParser(
        parser: UnsupportedExportRecordParser,
    ): ExportRecordParser

    companion object {
        @Provides
        @Singleton
        fun provideClock(): Clock = Clock.systemDefaultZone()

        @Provides
        fun provideContentResolver(
            @ApplicationContext context: Context,
        ): ContentResolver = context.contentResolver

        @Provides
        @Singleton
        fun provideDatabase(
            @ApplicationContext context: Context,
        ): BabyCacheDatabase = Room.databaseBuilder(
            context,
            BabyCacheDatabase::class.java,
            "baby_cache.db",
        ).fallbackToDestructiveMigration().build()

        @Provides
        fun provideBabyCacheDao(database: BabyCacheDatabase): BabyCacheDao = database.babyCacheDao()

        @Provides
        @Singleton
        fun provideSafeZipReader(): SafeZipReader = SafeZipReader()

        @Provides
        @Singleton
        fun provideBabyDataProvider(
            mock: MockBabyDataProvider,
            lab: LabBabyDataProvider,
            partnerApi: PartnerApiBabyDataProvider,
        ): BabyDataProvider = when (BuildConfig.FLAVOR) {
            "demo" -> mock
            "lab" -> lab
            else -> partnerApi
        }
    }
}
