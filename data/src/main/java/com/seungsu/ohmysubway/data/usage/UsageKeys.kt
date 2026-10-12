package com.seungsu.ohmysubway.data.usage

/**
 * 호출 주소(호스트)를 배포 서버(apk-dist)의 외부 API 이름으로 분류한다.
 * 서버의 lib/apis.js 의 EXTERNAL 과 같은 표 — 한쪽을 고치면 다른 쪽도 고친다.
 * 표에 없는 호스트는 null — 세지 않는다.
 */
object UsageKeys {
    fun forHost(host: String): String? = when {
        host == "swopenapi.seoul.go.kr" -> "subway-realtime"
        host == "api.groq.com" -> "stt-groq"
        host == "googleapis.com" || host.endsWith(".googleapis.com") -> "drive-backup"
        else -> null
    }
}
