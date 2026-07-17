package com.babyvoice.bridge.core.model

import kotlinx.coroutines.flow.Flow

data class ProviderInfo(
    val id: String,
    val displayName: String,
    val flavor: String,
    val state: ProviderState,
)

enum class ProviderState {
    READY,
    NEEDS_PERMISSION,
    NOT_CONFIGURED,
    UNSUPPORTED,
    ERROR,
}

interface BabyDataProvider {
    val providerInfo: ProviderInfo
    fun observeBabies(): Flow<List<BabyProfile>>
    fun observeSnapshot(babyId: BabyId): Flow<BabyCareSnapshot>
    suspend fun refresh(babyId: BabyId): Result<BabyCareSnapshot>
}

sealed class BabyDataFailure(
    open val safeMessage: String,
) : IllegalStateException(safeMessage) {
    data object AuthenticationRequired : BabyDataFailure("authentication_required")

    data object PermissionDenied : BabyDataFailure("permission_denied")

    data object NoData : BabyDataFailure("no_data")

    data object StaleData : BabyDataFailure("stale_data")

    data object ParseFailure : BabyDataFailure("parse_failure")

    data object NotConfigured : BabyDataFailure("not_configured")

    data object Unsupported : BabyDataFailure("unsupported")

    data object Unknown : BabyDataFailure("unknown")
}

