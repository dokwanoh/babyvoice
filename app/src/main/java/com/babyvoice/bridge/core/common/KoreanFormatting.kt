package com.babyvoice.bridge.core.common

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val koreanLocale: Locale = Locale("ko", "KR")

fun formatKoreanClockTime(instant: Instant, zoneId: ZoneId): String {
    val localDateTime = LocalDateTime.ofInstant(instant, zoneId)
    return when {
        localDateTime.hour == 0 && localDateTime.minute == 0 -> "자정"
        localDateTime.hour == 12 && localDateTime.minute == 0 -> "정오"
        else -> {
            val period = if (localDateTime.hour < 12) "오전" else "오후"
            val hour = localDateTime.hour % 12
            val displayHour = if (hour == 0) 12 else hour
            val minute = localDateTime.minute.toString().padStart(2, '0')
            "$period ${displayHour}시 ${minute}분"
        }
    }
}

fun formatRelativeDuration(duration: Duration): String {
    val totalMinutes = duration.abs().toMinutes()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return buildString {
        if (hours > 0) append("${hours}시간 ")
        append("${minutes}분")
        append(if (duration.isNegative) " 후" else " 전")
    }.trim()
}

fun formatAmountMl(amountMl: Int): String = "${amountMl}밀리리터"

fun formatDurationToSentence(duration: Duration): String {
    val absolute = duration.abs()
    val hours = absolute.toHours()
    val minutes = absolute.minusHours(hours).toMinutes()
    return when {
        hours > 0 && minutes > 0 -> "${hours}시간 ${minutes}분"
        hours > 0 -> "${hours}시간"
        else -> "${minutes}분"
    }
}

fun formatRelativeTimeFromNow(instant: Instant, now: Instant): String {
    val diff = Duration.between(instant, now)
    return formatRelativeDuration(diff)
}

fun formatRefreshTimestamp(instant: Instant, zoneId: ZoneId): String {
    val formatter = DateTimeFormatter.ofPattern("a h시 mm분", koreanLocale)
    return LocalDateTime.ofInstant(instant, zoneId).format(formatter)
}
