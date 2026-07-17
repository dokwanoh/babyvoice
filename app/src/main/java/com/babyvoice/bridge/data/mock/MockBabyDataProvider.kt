package com.babyvoice.bridge.data.mock

import com.babyvoice.bridge.core.common.UserSettings
import com.babyvoice.bridge.core.common.DemoScenario
import com.babyvoice.bridge.core.model.BabyCareSnapshot
import com.babyvoice.bridge.core.model.BabyDataFailure
import com.babyvoice.bridge.core.model.BabyDataProvider
import com.babyvoice.bridge.core.model.BabyId
import com.babyvoice.bridge.core.model.BabyProfile
import com.babyvoice.bridge.core.model.ProviderInfo
import com.babyvoice.bridge.core.model.ProviderState
import com.babyvoice.bridge.core.model.SyncStatus
import com.babyvoice.bridge.feature.settings.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Clock

@Singleton
class MockBabyDataProvider @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val cache: RoomBabyCache,
    private val clock: Clock,
) : BabyDataProvider {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val generator = DemoSnapshotGenerator(clock)
    private val snapshotState = MutableStateFlow<BabyCareSnapshot?>(null)
    private val settingsState = settingsRepository.settings.stateIn(
        scope,
        SharingStarted.Eagerly,
        UserSettings(),
    )
    private val currentProviderState = MutableStateFlow(ProviderState.READY)

    override val providerInfo: ProviderInfo
        get() = ProviderInfo(
            id = "mock",
            displayName = "데모 데이터",
            flavor = "demo",
            state = currentProviderState.value,
        )

    init {
        scope.launch {
            val bundle = generator.generate(settingsState.value.demoScenario, settingsState.value)
            cache.upsertBabies(bundle.babies, bundle.snapshot.fetchedAt.toEpochMilli())
            snapshotState.value = bundle.snapshot
        }
    }

    override fun observeBabies(): Flow<List<BabyProfile>> = cache.observeBabies()
        .combine(settingsState) { babies, settings ->
            if (babies.isEmpty()) {
                generator.generate(settings.demoScenario, settings).babies
            } else {
                babies
            }
        }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun observeSnapshot(babyId: BabyId): Flow<BabyCareSnapshot> =
        settingsState.combine(snapshotState) { settings, snapshot ->
            val latest = snapshot ?: generator.generate(settings.demoScenario, settings).snapshot
            if (latest.baby.id == babyId) latest else latest.copy(baby = latest.baby.copy(id = babyId))
        }

    override suspend fun refresh(babyId: BabyId): Result<BabyCareSnapshot> {
        return withContext(Dispatchers.Default) {
            val bundle = generator.generate(settingsState.value.demoScenario, settingsState.value)
            cache.upsertBabies(bundle.babies, bundle.snapshot.fetchedAt.toEpochMilli())
            snapshotState.value = bundle.snapshot.copy(baby = bundle.snapshot.baby.copy(id = babyId))
            currentProviderState.value = when (bundle.snapshot.syncStatus) {
                is SyncStatus.Failed -> when (bundle.snapshot.syncStatus.failure) {
                    BabyDataFailure.PermissionDenied -> ProviderState.NEEDS_PERMISSION
                    BabyDataFailure.NotConfigured -> ProviderState.NOT_CONFIGURED
                    BabyDataFailure.Unsupported -> ProviderState.UNSUPPORTED
                    else -> ProviderState.ERROR
                }
                SyncStatus.NotConfigured -> ProviderState.NOT_CONFIGURED
                SyncStatus.Unsupported -> ProviderState.UNSUPPORTED
                else -> ProviderState.READY
            }
            when (bundle.snapshot.syncStatus) {
                is SyncStatus.Failed -> Result.failure(bundle.snapshot.syncStatus.failure)
                else -> Result.success(bundle.snapshot)
            }
        }
    }
}
