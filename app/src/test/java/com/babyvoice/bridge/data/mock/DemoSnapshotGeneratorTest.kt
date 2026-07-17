package com.babyvoice.bridge.data.mock

import com.babyvoice.bridge.core.common.DemoScenario
import com.babyvoice.bridge.core.common.UserSettings
import com.babyvoice.bridge.core.model.DataOrigin
import com.babyvoice.bridge.core.model.FeedingType
import java.time.Clock
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DemoSnapshotGeneratorTest {
    private val zone = ZoneId.of("Asia/Seoul")
    private val now = ZonedDateTime.of(2026, 7, 15, 15, 20, 0, 0, zone).toInstant()
    private val generator = DemoSnapshotGenerator(Clock.fixed(now, zone))

    @Test
    fun preservesOriginMetadataAndScenarioVariations() {
        val bundle = generator.generate(
            scenario = DemoScenario.MULTIPLE_BABIES,
            settings = UserSettings(
                selectedBabyId = "seojun",
                usePatternEstimate = true,
            ),
        )

        assertEquals(2, bundle.babies.size)
        assertEquals("seojun", bundle.snapshot.baby.id.value)
        assertEquals(DataOrigin.MOCK, bundle.snapshot.lastFeeding?.origin)
        assertEquals(DataOrigin.LOCAL_PATTERN_ESTIMATE, bundle.snapshot.nextFeedingTime?.origin)
        assertEquals(DataOrigin.LOCAL_PATTERN_ESTIMATE, bundle.snapshot.nextSleepAt?.origin)
        assertEquals(FeedingType.FORMULA, bundle.snapshot.lastFeeding?.value?.feedingType)
        assertEquals(now.minus(Duration.ofMinutes(32)), bundle.snapshot.lastFeeding?.value?.startedAt)
        assertEquals(now.minus(Duration.ofMinutes(24)), bundle.snapshot.lastFeeding?.value?.endedAt)
        assertEquals(now.minus(Duration.ofMinutes(6)), bundle.snapshot.lastWakeAt?.value)
        assertEquals(now.plus(Duration.ofHours(2)).plus(Duration.ofMinutes(31)), bundle.snapshot.nextFeedingTime?.value)
        assertEquals(now.plus(Duration.ofMinutes(33)), bundle.snapshot.nextSleepAt?.value)
        assertEquals(now.minus(Duration.ofHours(1)).minus(Duration.ofMinutes(29)), bundle.snapshot.lastDiaper?.value?.changedAt)
    }

    @Test
    fun normalScenario_matches_the_widget_capture_values() {
        val bundle = generator.generate(
            scenario = DemoScenario.NORMAL,
            settings = UserSettings(),
        )

        assertEquals("오서은", bundle.snapshot.baby.name)
        assertEquals(now.minus(Duration.ofMinutes(32)), bundle.snapshot.lastFeeding?.value?.startedAt)
        assertEquals(630, bundle.snapshot.lastFeeding?.value?.amountMl)
        assertEquals(now.plus(Duration.ofHours(2)).plus(Duration.ofMinutes(31)), bundle.snapshot.nextFeedingTime?.value)
        assertEquals(now.minus(Duration.ofMinutes(6)), bundle.snapshot.lastWakeAt?.value)
        assertEquals(now.plus(Duration.ofMinutes(33)), bundle.snapshot.nextSleepAt?.value)
        assertEquals(now.minus(Duration.ofHours(1)).minus(Duration.ofMinutes(29)), bundle.snapshot.lastDiaper?.value?.changedAt)
    }

    @Test
    fun omitsNextFeedingAmountWhenScenarioRequestsIt() {
        val bundle = generator.generate(
            scenario = DemoScenario.NO_NEXT_FEEDING_AMOUNT,
            settings = UserSettings(),
        )

        assertNull(bundle.snapshot.nextFeedingAmountMl)
    }
}
