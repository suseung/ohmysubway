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
) {
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
}
