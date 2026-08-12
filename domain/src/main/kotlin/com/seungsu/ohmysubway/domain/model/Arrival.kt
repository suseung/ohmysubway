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
    /** trainLineName("성수행 - 구의방면")에서 다음 정차역("구의")을 추출한다. */
    val nextStationName: String
        get() = trainLineName
            .substringAfterLast("-")
            .trim()
            .removeSuffix("방면")
            .trim()
}
