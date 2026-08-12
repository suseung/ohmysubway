package com.seungsu.ohmysubway.domain.util

/**
 * API 역명(병기 포함)을 화면 표시용 이름으로 바꾼다.
 * 예: "총신대입구(이수)" → "총신대입구", "응암순환(상선)" → "응암"
 */
val String.stationDisplayName: String
    get() = when (this) {
        "응암순환(상선)" -> "응암"
        else -> substringBefore("(").trim()
    }
