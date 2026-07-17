package com.babyvoice.bridge.data.partnerapi

import com.babyvoice.bridge.core.model.BabyId
import com.babyvoice.bridge.core.model.BabyProfile
import com.babyvoice.bridge.core.model.BabyCareSnapshot

import javax.inject.Inject

class PartnerApiMapper @Inject constructor() {
    fun toDomain(baby: PartnerBabyDto): BabyProfile = BabyProfile(
        id = BabyId(baby.id),
        name = baby.name,
        isPrimary = baby.isPrimary,
        isTwin = baby.isTwin,
    )

    fun toDomain(snapshot: PartnerSnapshotDto): BabyCareSnapshot = snapshot.snapshot.copy(
        baby = toDomain(snapshot.baby),
    )
}
