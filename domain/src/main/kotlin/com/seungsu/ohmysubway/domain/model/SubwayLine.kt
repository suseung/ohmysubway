package com.seungsu.ohmysubway.domain.model

data class SubwayLine(
    val subwayId: String,
    val name: String,
    val routes: List<SubwayRoute>,
)

data class SubwayRoute(
    val name: String,
    val circular: Boolean,
    val stations: List<String>,
)
