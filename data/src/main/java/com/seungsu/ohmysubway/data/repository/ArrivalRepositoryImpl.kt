package com.seungsu.ohmysubway.data.repository

import com.seungsu.ohmysubway.data.BuildConfig
import com.seungsu.ohmysubway.data.model.RealtimeArrivalDto
import com.seungsu.ohmysubway.data.service.SubwayApiService
import com.seungsu.ohmysubway.domain.model.Arrival
import com.seungsu.ohmysubway.domain.repository.ArrivalRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArrivalRepositoryImpl @Inject constructor(
    private val subwayApiService: SubwayApiService,
) : ArrivalRepository {

    override suspend fun getArrivals(stationName: String): List<Arrival> {
        val response = subwayApiService.getRealtimeArrivals(
            apiKey = BuildConfig.SEOUL_SUBWAY_API_KEY,
            count = ARRIVAL_FETCH_COUNT,
            stationName = stationName,
        )

        val error = response.errorMessage
        if (error != null && error.code !in SUCCESS_CODES) {
            if (error.code == CODE_NO_DATA) return emptyList()
            throw IllegalStateException("지하철 도착정보 조회 실패 [${error.code}] ${error.message}")
        }
        if (response.status != null && response.code != null) {
            if (response.code == CODE_NO_DATA) return emptyList()
            throw IllegalStateException("지하철 도착정보 조회 실패 [${response.code}] ${response.message.orEmpty()}")
        }

        return response.realtimeArrivalList.map { it.toDomain() }
    }

    private fun RealtimeArrivalDto.toDomain() = Arrival(
        subwayId = subwayId,
        stationName = statnNm,
        updnLine = updnLine,
        trainLineName = trainLineNm,
        terminalStation = bstatnNm,
        secondsToArrival = barvlDt.toIntOrNull() ?: 0,
        arrivalMessage = arvlMsg2,
        arrivalCode = arvlCd,
        trainStatus = btrainSttus,
        receivedAt = recptnDt,
    )

    companion object {
        private const val ARRIVAL_FETCH_COUNT = 100
        private const val CODE_NO_DATA = "INFO-200"
        private val SUCCESS_CODES = setOf("INFO-000")
    }
}
