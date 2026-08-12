package com.seungsu.ohmysubway.common.util

/** 남은 초를 사람이 읽는 문구로 바꾼다. */
fun formatRemaining(seconds: Int): String = when {
    seconds <= IMMINENT_SECONDS -> "곧 도착"
    seconds < SECONDS_PER_MINUTE -> "${seconds}초 후"
    else -> {
        val minutes = seconds / SECONDS_PER_MINUTE
        val rest = seconds % SECONDS_PER_MINUTE
        if (rest == 0) "${minutes}분 후" else "${minutes}분 ${rest}초 후"
    }
}

const val IMMINENT_SECONDS = 20
private const val SECONDS_PER_MINUTE = 60
