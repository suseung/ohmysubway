package com.seungsu.ohmysubway.common.util

/** 남은 초를 사람이 읽는 문구로 바꾼다. */
fun formatRemaining(seconds: Int): String = when {
    seconds <= 0 -> "0초"
    seconds < SECONDS_PER_MINUTE -> "${seconds}초 후"
    else -> {
        val minutes = seconds / SECONDS_PER_MINUTE
        val rest = seconds % SECONDS_PER_MINUTE
        if (rest == 0) "${minutes}분 후" else "${minutes}분 ${rest}초 후"
    }
}

/** 위젯 카운트다운(Chronometer)과 같은 분:초 형식. 멈춘 값도 같은 모양으로 보이게 쓴다. */
fun formatMinutesSeconds(seconds: Long): String {
    val safe = seconds.coerceAtLeast(0)
    return "%02d:%02d".format(safe / SECONDS_PER_MINUTE, safe % SECONDS_PER_MINUTE)
}

private const val SECONDS_PER_MINUTE = 60
