package com.seungsu.ohmysubway.domain.model

data class Arrival(
    val subwayId: String,
    val stationName: String,
    val updnLine: String,
    val trainLineName: String,
    val terminalStation: String,
    val secondsToArrival: Int,
    val arrivalMessage: String,
    val arrivalCode: String,
    val trainStatus: String,
    val receivedAt: String,
    /** API가 이 정보를 만든 시각. 데이터가 낡은 만큼 남은 시간을 보정하는 데 쓴다. */
    val receivedAtMillis: Long,
) {
    /**
     * 데이터 지연을 보정한 남은 시간(초).
     *
     * API는 30초 주기 스냅샷을 주고, 받는 시점엔 이미 40초 이상 묵어 있다.
     * 초 단위 정보(barvlDt)를 주지 않는 노선(경의중앙·공항철도 등)은 null.
     */
    fun remainingSeconds(nowMillis: Long): Int? {
        if (secondsToArrival <= 0) return null
        // 사업자 시계가 앞서 있으면(신분당선) 지연이 음수로 나오므로 0으로 막는다
        val stalenessSeconds = ((nowMillis - receivedAtMillis) / MILLIS_PER_SECOND).coerceAtLeast(0)
        return (secondsToArrival - stalenessSeconds).coerceAtLeast(0).toInt()
    }

    /**
     * trainLineName에서 다음 정차역을 추출한다.
     * 예: "성수행 - 구의방면" → "구의", "구로행 - 신도림방면 (급행)" → "신도림"
     */
    val nextStationName: String
        get() {
            val tail = trainLineName.substringAfterLast("-")
            if ("방면" !in tail) return ""
            return tail.substringBefore("방면").trim()
        }

    private companion object {
        const val MILLIS_PER_SECOND = 1000L
    }
}
