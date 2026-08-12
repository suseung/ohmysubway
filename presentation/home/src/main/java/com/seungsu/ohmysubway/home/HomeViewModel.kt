package com.seungsu.ohmysubway.home

import com.seungsu.ohmysubway.core.mvi.MVIViewModel
import com.seungsu.ohmysubway.domain.model.DirectedArrivals
import com.seungsu.ohmysubway.domain.model.StationSummary
import com.seungsu.ohmysubway.domain.usecase.GetDirectedArrivalsUseCase
import com.seungsu.ohmysubway.domain.usecase.SearchStationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchStations: SearchStationsUseCase,
    private val getDirectedArrivals: GetDirectedArrivalsUseCase,
) : MVIViewModel<HomeIntent, HomeState, HomeEffect>() {

    override fun createInitialState() = HomeState()

    override suspend fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.UpdateStartQuery -> {
                setState { copy(startQuery = intent.query, selectedStart = null) }
                val results = search(intent.query)
                setState { copy(startResults = results) }
            }

            is HomeIntent.UpdateDestQuery -> {
                setState { copy(destQuery = intent.query, selectedDest = null) }
                val results = search(intent.query)
                setState { copy(destResults = results) }
            }

            is HomeIntent.SelectStart -> setState {
                copy(startQuery = intent.station.name, selectedStart = intent.station.name, startResults = emptyList())
            }

            is HomeIntent.SelectDest -> setState {
                copy(destQuery = intent.station.name, selectedDest = intent.station.name, destResults = emptyList())
            }

            HomeIntent.Lookup -> lookup()
        }
    }

    private suspend fun search(query: String): List<StationSummary> =
        if (query.isBlank()) emptyList() else searchStations(query)

    private suspend fun lookup() {
        val start = currentState { selectedStart } ?: return
        val dest = currentState { selectedDest } ?: return

        setState { copy(isLoading = true, errorMessage = null) }
        runCatching {
            getDirectedArrivals(GetDirectedArrivalsUseCase.Params(start, dest))
        }.fold(
            onSuccess = { result ->
                when (result) {
                    is DirectedArrivals.NotConnected -> setState {
                        copy(
                            isLoading = false,
                            lookedUp = true,
                            arrivals = emptyList(),
                            errorMessage = "두 역이 같은 노선으로 연결되어 있지 않아요. 환승 없는 구간만 지원해요.",
                        )
                    }

                    is DirectedArrivals.Success -> setState {
                        copy(isLoading = false, lookedUp = true, arrivals = result.arrivals, errorMessage = null)
                    }
                }
            },
            onFailure = {
                setState { copy(isLoading = false, lookedUp = true, errorMessage = "도착정보를 불러오지 못했어요. 잠시 후 다시 시도해주세요.") }
            },
        )
    }
}
