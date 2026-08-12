package com.seungsu.ohmysubway.domain.usecase

import com.seungsu.ohmysubway.domain.di.IoDispatcher
import com.seungsu.ohmysubway.domain.model.DirectedArrival
import com.seungsu.ohmysubway.domain.model.DirectedArrivals
import com.seungsu.ohmysubway.domain.repository.ArrivalRepository
import com.seungsu.ohmysubway.domain.repository.SubwayLineRepository
import com.seungsu.ohmysubway.domain.util.DirectionResolver
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetDirectedArrivalsUseCase @Inject constructor(
    private val subwayLineRepository: SubwayLineRepository,
    private val arrivalRepository: ArrivalRepository,
    @IoDispatcher dispatcher: CoroutineDispatcher,
) : UseCase<GetDirectedArrivalsUseCase.Params, DirectedArrivals>(dispatcher) {

    data class Params(
        val startStation: String,
        val destinationStation: String,
    )

    override suspend fun execute(params: Params): DirectedArrivals {
        val lines = subwayLineRepository.getLines()
        val directions = DirectionResolver.resolve(lines, params.startStation, params.destinationStation)
        if (directions.isEmpty()) return DirectedArrivals.NotConnected

        val lineNameById = lines.associate { it.subwayId to it.name }
        val arrivals = arrivalRepository.getArrivals(params.startStation)
            .filter { arrival -> directions.any { DirectionResolver.matches(it, arrival) } }
            .sortedBy { it.secondsToArrival }
            .map { DirectedArrival(lineName = lineNameById[it.subwayId] ?: it.subwayId, arrival = it) }

        return DirectedArrivals.Success(
            startStation = params.startStation,
            destinationStation = params.destinationStation,
            arrivals = arrivals,
        )
    }
}
