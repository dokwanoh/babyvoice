package com.babyvoice.bridge.data.notification

import com.babyvoice.bridge.core.model.DataOrigin
import com.babyvoice.bridge.core.model.BabyId
import com.babyvoice.bridge.core.model.DiaperType
import com.babyvoice.bridge.core.model.FeedingType
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BabyTimeNotificationFieldParserTest {
    private val clock = Clock.fixed(Instant.parse("2026-07-17T13:00:00Z"), ZoneId.of("Asia/Seoul"))
    private val parser = BabyTimeNotificationFieldParser(clock)

    @Test
    fun parsesDisplayedBabyTimeNotificationFieldsConservatively() {
        val result = parser.parse(
            packageName = "yducky.application.babytime",
            title = "BabyTime",
            text = """
                분유 32분 전 120ml
                다음 수유: 2시간 31분 남음
                낮잠 6분 전
                소변 1시간 29분 전
            """.trimIndent(),
        )

        assertTrue(result is NotificationParseResult.Parsed)
        val snapshot = (result as NotificationParseResult.Parsed).snapshot
        assertEquals(FeedingType.FORMULA, snapshot.lastFeeding?.value?.feedingType)
        assertEquals(120, snapshot.lastFeeding?.value?.amountMl)
        assertEquals(Instant.parse("2026-07-17T12:28:00Z"), snapshot.lastFeeding?.observedAt)
        assertEquals(Instant.parse("2026-07-17T15:31:00Z"), snapshot.nextFeedingTime?.value)
        assertEquals(true, snapshot.currentSleep?.isCurrentlySleeping)
        assertEquals(DiaperType.URINE, snapshot.lastDiaper?.value?.diaperType)
        assertEquals(DataOrigin.NOTIFICATION, snapshot.lastFeeding?.origin)
    }

    @Test
    fun extractsBabyNameFromNotificationTitleWhenPresent() {
        val result = parser.parse(
            packageName = "yducky.application.babytime",
            title = "양호해요 (오서은)",
            text = "분유 32분 전 120ml",
        )

        assertTrue(result is NotificationParseResult.Parsed)
        val snapshot = (result as NotificationParseResult.Parsed).snapshot
        assertEquals("오서은", snapshot.baby.name)
        assertEquals(BabyId("babytime-notification:오서은"), snapshot.baby.id)
    }

    @Test
    fun ignoresOtherPackagesAndUnrecognizedText() {
        assertEquals(
            NotificationParseResult.Unsupported,
            parser.parse("other.package", "BabyTime", "분유 10분 전"),
        )
        assertEquals(
            NotificationParseResult.Unsupported,
            parser.parse("yducky.application.babytime", "BabyTime", "일반 알림"),
        )
    }
}
