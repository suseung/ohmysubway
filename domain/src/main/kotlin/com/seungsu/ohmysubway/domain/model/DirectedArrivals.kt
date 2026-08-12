package com.seungsu.ohmysubway.domain.model

sealed interface DirectedArrivals {

    /** 시작역과 도착역이 같은 노선으로 이어져 있지 않음 */
    data object NotConnected : DirectedArrivals

    data class Success(
        val startStation: String,
        val destinationStation: String,
        val arrivals: List<DirectedArrival>,
    ) : DirectedArrivals
}

data class DirectedArrival(
    val lineName: String,
    val arrival: Arrival,
)
