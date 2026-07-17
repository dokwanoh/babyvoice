package com.babyvoice.bridge.integration.assistant

import com.babyvoice.bridge.core.model.BabyDataFailure
import com.babyvoice.bridge.core.model.DataOrigin
import com.babyvoice.bridge.core.model.DiaperType
import com.babyvoice.bridge.core.model.FeedingType
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BabyTimeWidgetSnapshotParserTest {
    private val clock = Clock.fixed(Instant.parse("2026-07-17T13:00:00Z"), ZoneId.of("Asia/Seoul"))
    private val parser = BabyTimeWidgetSnapshotParser(clock)

    @Test
    fun parses_visible_widget_tree_fields_without_ocr() {
        val result = parser.parse(
            """
            id=yducky.application.babytime:id/tvBabyName text=오서은
            id=yducky.application.babytime:id/tv1stTitle text=분유
            id=yducky.application.babytime:id/tv1stDelay text=32분
            id=yducky.application.babytime:id/tv1stAdd text=전
            id=yducky.application.babytime:id/tv1st_extra_info_prefix text=다음 수유:
            id=yducky.application.babytime:id/tv1st_extra_info text=2시간 31분 남음
            id=yducky.application.babytime:id/tv2ndTitle text=분유
            id=yducky.application.babytime:id/tv2ndDelay text=630
            id=yducky.application.babytime:id/tv2ndAdd text=ml
            id=yducky.application.babytime:id/tv3rdTitle text=낮잠
            id=yducky.application.babytime:id/tv3rdDelay text=6분
            id=yducky.application.babytime:id/tv3rdAdd text=전
            id=yducky.application.babytime:id/tv4thTitle text=소변
            id=yducky.application.babytime:id/tv4thDelay text=1시간 29분
            id=yducky.application.babytime:id/tv4thAdd text=전
            id=yducky.application.babytime:id/tvTotalAmountOfToday text=630ml(580+50)
            """.trimIndent(),
        )

        assertTrue(result.isSuccess)
        val snapshot = result.getOrThrow()
        assertEquals("오서은", snapshot.baby.name)
        assertEquals(FeedingType.FORMULA, snapshot.lastFeeding?.value?.feedingType)
        assertEquals(630, snapshot.lastFeeding?.value?.amountMl)
        assertEquals(Instant.parse("2026-07-17T12:28:00Z"), snapshot.lastFeeding?.observedAt)
        assertEquals(Instant.parse("2026-07-17T15:31:00Z"), snapshot.nextFeedingTime?.value)
        assertEquals(DiaperType.URINE, snapshot.lastDiaper?.value?.diaperType)
        assertEquals(Instant.parse("2026-07-17T11:31:00Z"), snapshot.lastDiaper?.observedAt)
        assertEquals(DataOrigin.ACCESSIBILITY_WIDGET, snapshot.lastFeeding?.origin)
        assertFalse(snapshot.currentSleep?.isCurrentlySleeping ?: false)
    }

    @Test
    fun marks_explicit_sleep_status_as_current_sleep() {
        val result = parser.parse(
            """
            id=yducky.application.babytime:id/tvBabyName text=오서은
            id=yducky.application.babytime:id/tvSleepStatus text=수면 중
            id=yducky.application.babytime:id/tvSleepStatusDuration text=6분
            """.trimIndent(),
        )

        assertTrue(result.isSuccess)
        val snapshot = result.getOrThrow()
        assertTrue(snapshot.currentSleep?.isCurrentlySleeping == true)
        assertEquals(Instant.parse("2026-07-17T12:54:00Z"), snapshot.currentSleep?.startedAt)
    }

    @Test
    fun rejects_unrelated_text_without_widget_fields() {
        val result = parser.parse("id=other.package:id/title text=hello")

        assertTrue(result.isFailure)
        assertEquals(BabyDataFailure.Unsupported, result.exceptionOrNull())
    }
}
