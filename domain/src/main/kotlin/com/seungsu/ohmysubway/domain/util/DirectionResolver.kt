package com.seungsu.ohmysubway.domain.util

import com.seungsu.ohmysubway.domain.model.Arrival
import com.seungsu.ohmysubway.domain.model.SubwayLine
import com.seungsu.ohmysubway.domain.model.SubwayRoute

/**
 * 시작역 → 도착역 방향의 열차인지 판별하기 위한 노선 내 방향 정보.
 *
 * @param step 노선 역 목록에서 도착역 쪽으로 이동하는 인덱스 증감 방향 (+1 / -1)
 */
data class LineDirection(
    val subwayId: String,
    val lineName: String,
    val route: SubwayRoute,
    val startIndex: Int,
    val destIndex: Int,
    val step: Int,
) {
    /** 시작역에서 도착역 방향의 바로 다음 역 이름 */
    val expectedNextStation: String
        get() {
            val stations = route.stations
            val next = if (route.circular) {
                (startIndex + step + stations.size) % stations.size
            } else {
                startIndex + step
            }
            return stations[next]
        }
}

object DirectionResolver {

    /** 시작역과 도착역을 모두 지나는 노선(route)들의 방향 정보를 구한다. */
    fun resolve(lines: List<SubwayLine>, start: String, dest: String): List<LineDirection> =
        lines.flatMap { line ->
            line.routes.mapNotNull { route ->
                val startIndex = route.stations.indexOf(start)
                val destIndex = route.stations.indexOf(dest)
                if (startIndex < 0 || destIndex < 0 || startIndex == destIndex) return@mapNotNull null

                val step = directionSign(route, startIndex, destIndex)
                if (step == 0) return@mapNotNull null

                LineDirection(
                    subwayId = line.subwayId,
                    lineName = line.name,
                    route = route,
                    startIndex = startIndex,
                    destIndex = destIndex,
                    step = step,
                )
            }
        }.distinctBy { it.subwayId to it.expectedNextStation }

    /** 도착 정보의 열차가 해당 방향으로 가는 열차인지 판별한다. */
    fun matches(direction: LineDirection, arrival: Arrival): Boolean {
        if (arrival.subwayId != direction.subwayId) return false
        val stations = direction.route.stations

        // 1순위: 다음 정차역("~방면")으로 판별 — 급행이 역을 건너뛰어도 방향 부호는 유지된다
        val nextIndex = stations.indexOf(arrival.nextStationName)
        if (nextIndex >= 0 && nextIndex != direction.startIndex) {
            return directionSign(direction.route, direction.startIndex, nextIndex) == direction.step
        }

        // 2순위: 종착역 방향으로 판별
        val terminalIndex = stations.indexOf(arrival.terminalStation)
        if (terminalIndex >= 0 && terminalIndex != direction.startIndex) {
            return directionSign(direction.route, direction.startIndex, terminalIndex) == direction.step
        }
        return false
    }

    /**
     * from → to 방향 부호. 순환선은 짧은 호 방향을 선택한다.
     */
    private fun directionSign(route: SubwayRoute, fromIndex: Int, toIndex: Int): Int {
        if (fromIndex == toIndex) return 0
        if (!route.circular) return if (toIndex > fromIndex) 1 else -1

        val size = route.stations.size
        val forward = (toIndex - fromIndex + size) % size
        val backward = size - forward
        return if (forward <= backward) 1 else -1
    }
}
