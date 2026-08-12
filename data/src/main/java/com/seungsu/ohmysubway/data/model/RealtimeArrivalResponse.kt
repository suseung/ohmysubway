package com.seungsu.ohmysubway.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeArrivalResponse(
    val errorMessage: ErrorMessage? = null,
    val realtimeArrivalList: List<RealtimeArrivalDto> = emptyList(),
    // 데이터가 없거나 오류일 때는 아래 필드로 내려온다
    val status: Int? = null,
    val code: String? = null,
    val message: String? = null,
)

@Serializable
data class ErrorMessage(
    val status: Int = 0,
    val code: String = "",
    val message: String = "",
    val total: Int = 0,
)

@Serializable
data class RealtimeArrivalDto(
    val subwayId: String = "",
    val updnLine: String = "",
    val trainLineNm: String = "",
    val statnNm: String = "",
    val bstatnNm: String = "",
    val barvlDt: String = "0",
    val arvlMsg2: String = "",
    val arvlMsg3: String = "",
    val arvlCd: String = "",
    val btrainSttus: String = "",
    val btrainNo: String = "",
    val recptnDt: String = "",
)
