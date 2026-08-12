package com.seungsu.ohmysubway.domain.usecase

import com.seungsu.ohmysubway.domain.di.IoDispatcher
import com.seungsu.ohmysubway.domain.model.StationSummary
import com.seungsu.ohmysubway.domain.repository.SubwayLineRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class SearchStationsUseCase @Inject constructor(
    private val subwayLineRepository: SubwayLineRepository,
    @IoDispatcher dispatcher: CoroutineDispatcher,
) : UseCase<String, List<StationSummary>>(dispatcher) {

    override suspend fun execute(params: String): List<StationSummary> {
        val query = params.trim()
        if (query.isEmpty()) return emptyList()

        val lineNamesByStation = linkedMapOf<String, MutableList<String>>()
        subwayLineRepository.getLines().forEach { line ->
            line.routes.forEach { route ->
                route.stations.forEach { station ->
                    val lineNames = lineNamesByStation.getOrPut(station) { mutableListOf() }
                    if (line.name !in lineNames) lineNames += line.name
                }
            }
        }

        val matched = lineNamesByStation.filterKeys { it.contains(query) }.ifEmpty {
            // "서울역"처럼 '역'을 붙여 검색하는 경우 대비
            if (query.length > 1 && query.endsWith("역")) {
                val trimmed = query.dropLast(1)
                lineNamesByStation.filterKeys { it.contains(trimmed) }
            } else {
                emptyMap()
            }
        }

        return matched
            .map { (name, lineNames) -> StationSummary(name = name, lineNames = lineNames) }
            .sortedWith(compareByDescending<StationSummary> { it.name.startsWith(query) }.thenBy { it.name })
    }
}
