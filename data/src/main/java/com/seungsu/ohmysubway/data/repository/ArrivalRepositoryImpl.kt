package com.seungsu.ohmysubway.data.repository

import com.seungsu.ohmysubway.data.BuildConfig
import com.seungsu.ohmysubway.data.model.RealtimeArrivalDto
import com.seungsu.ohmysubway.data.service.SubwayApiService
import com.seungsu.ohmysubway.domain.model.Arrival
import com.seungsu.ohmysubway.domain.repository.ArrivalRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

    /** recptnDt는 한국 시간 문자열이라 기기 시간대와 무관하게 서울 기준으로 해석한다. */
    private fun parseReceivedAt(value: String): Long = runCatching {
        LocalDateTime.parse(value.trim(), RECEIVED_AT_FORMAT)
            .atZone(SEOUL_ZONE)
            .toInstant()
            .toEpochMilli()
    }.getOrDefault(0L)

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
        receivedAtMillis = parseReceivedAt(recptnDt),
    )

    companion object {
        private const val ARRIVAL_FETCH_COUNT = 100
        private const val CODE_NO_DATA = "INFO-200"
        private val SUCCESS_CODES = setOf("INFO-000")
        private val SEOUL_ZONE = ZoneId.of("Asia/Seoul")
        private val RECEIVED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}
