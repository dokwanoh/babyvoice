package com.babyvoice.bridge.di

import com.babyvoice.bridge.integration.assistant.BabyTimeUiAdapter
import com.babyvoice.bridge.integration.assistant.BabyTimeWidgetSnapshotRepository
import com.babyvoice.bridge.integration.assistant.InMemoryBabyTimeWidgetSnapshotRepository
import com.babyvoice.bridge.integration.assistant.LabBabyTimeUiAdapter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AssistantModule {
    @Binds
    abstract fun bindBabyTimeUiAdapter(
        adapter: LabBabyTimeUiAdapter,
    ): BabyTimeUiAdapter

    @Binds
    abstract fun bindBabyTimeWidgetSnapshotRepository(
        repository: InMemoryBabyTimeWidgetSnapshotRepository,
    ): BabyTimeWidgetSnapshotRepository
}
