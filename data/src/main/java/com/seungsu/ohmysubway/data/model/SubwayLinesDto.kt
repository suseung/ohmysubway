package com.seungsu.ohmysubway.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SubwayLinesDto(
    val lines: List<SubwayLineDto> = emptyList(),
)

@Serializable
data class SubwayLineDto(
    val subwayId: String,
    val name: String,
    val routes: List<SubwayRouteDto> = emptyList(),
)

@Serializable
data class SubwayRouteDto(
    val name: String,
    val circular: Boolean = false,
    val stations: List<String> = emptyList(),
)
