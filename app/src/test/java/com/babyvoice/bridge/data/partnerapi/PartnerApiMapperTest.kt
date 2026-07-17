package com.babyvoice.bridge.data.partnerapi

import com.babyvoice.bridge.core.model.BabyCareSnapshot
import com.babyvoice.bridge.core.model.BabyId
import com.babyvoice.bridge.core.model.BabyProfile
import com.babyvoice.bridge.core.model.SyncStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class PartnerApiMapperTest {
    private val mapper = PartnerApiMapper()

    @Test
    fun mapsNestedSnapshotBabyToDomainBaby() {
        val dto = PartnerSnapshotDto(
            baby = PartnerBabyDto(
                id = "haram",
                name = "하람",
                isPrimary = true,
                isTwin = false,
            ),
            snapshot = BabyCareSnapshot(
                baby = BabyProfile(BabyId("placeholder"), "가명", isPrimary = false),
                currentFeeding = null,
                lastFeeding = null,
                nextFeedingTime = null,
                nextFeedingAmountMl = null,
                currentSleep = null,
                lastWakeAt = null,
                nextSleepAt = null,
                lastDiaper = null,
                fetchedAt = Instant.parse("2026-07-15T06:20:00Z"),
                syncStatus = SyncStatus.NotConfigured,
            ),
        )

        val domain = mapper.toDomain(dto)

        assertEquals("haram", domain.baby.id.value)
        assertEquals("하람", domain.baby.name)
    }
}
