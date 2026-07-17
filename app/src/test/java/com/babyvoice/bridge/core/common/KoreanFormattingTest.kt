package com.babyvoice.bridge.core.common

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class KoreanFormattingTest {
    private val seoul = ZoneId.of("Asia/Seoul")

    @Test
    fun formatsClockTimeAndUnitsNaturally() {
        val afternoon = ZonedDateTime.of(2026, 7, 15, 14, 10, 0, 0, seoul).toInstant()
        val midnight = ZonedDateTime.of(2026, 7, 16, 0, 0, 0, 0, seoul).toInstant()

        assertEquals("오후 2시 10분", formatKoreanClockTime(afternoon, seoul))
        assertEquals("자정", formatKoreanClockTime(midnight, seoul))
        assertEquals("120밀리리터", formatAmountMl(120))
        assertEquals("1시간 35분", formatDurationToSentence(Duration.ofMinutes(95)))
        assertEquals("1시간 35분 전", formatRelativeDuration(Duration.ofMinutes(95)))
        assertEquals("15분 후", formatRelativeDuration(Duration.ofMinutes(-15)))
    }

    @Test
    fun formatsRefreshTimestampWithAmPm() {
        val instant = ZonedDateTime.of(2026, 7, 15, 9, 20, 0, 0, seoul).toInstant()
        assertEquals("오전 9시 20분", formatRefreshTimestamp(instant, seoul))
    }

    @Test
    fun formatsRelativeFromNow() {
        val now = Instant.parse("2026-07-15T06:20:00Z")
        val instant = now.minus(Duration.ofMinutes(95))
        assertEquals("1시간 35분 전", formatRelativeTimeFromNow(instant, now))
    }
}
