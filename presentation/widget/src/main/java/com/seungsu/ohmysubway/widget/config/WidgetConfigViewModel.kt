package com.seungsu.ohmysubway.widget.config

import com.seungsu.ohmysubway.core.mvi.MVIViewModel
import com.seungsu.ohmysubway.domain.model.StationSummary
import com.seungsu.ohmysubway.domain.repository.SubwayLineRepository
import com.seungsu.ohmysubway.domain.usecase.SearchStationsUseCase
import com.seungsu.ohmysubway.domain.util.DirectionResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WidgetConfigViewModel @Inject constructor(
    private val searchStations: SearchStationsUseCase,
    private val subwayLineRepository: SubwayLineRepository,
) : MVIViewModel<WidgetConfigIntent, WidgetConfigState, WidgetConfigEffect>() {

    override fun createInitialState() = WidgetConfigState()

    override suspend fun processIntent(intent: WidgetConfigIntent) {
        when (intent) {
            is WidgetConfigIntent.UpdateStartQuery -> {
                setState { copy(startQuery = intent.query, selectedStart = null, notConnected = false) }
                val results = search(intent.query)
                setState { copy(startResults = results) }
            }

            is WidgetConfigIntent.UpdateDestQuery -> {
                setState { copy(destQuery = intent.query, selectedDest = null, notConnected = false) }
                val results = search(intent.query)
                setState { copy(destResults = results) }
            }

            is WidgetConfigIntent.SelectStart -> {
                setState {
                    copy(startQuery = intent.station.name, selectedStart = intent.station.name, startResults = emptyList())
                }
                validateConnection()
            }

            is WidgetConfigIntent.SelectDest -> {
                setState {
                    copy(destQuery = intent.station.name, selectedDest = intent.station.name, destResults = emptyList())
                }
                validateConnection()
            }

            WidgetConfigIntent.Save -> save()
        }
    }

    private suspend fun search(query: String): List<StationSummary> =
        if (query.isBlank()) emptyList() else searchStations(query)

    private suspend fun validateConnection() {
        val start = currentState { selectedStart } ?: return
        val dest = currentState { selectedDest } ?: return
        if (start == dest) return

        val connected = DirectionResolver
            .resolve(subwayLineRepository.getLines(), start, dest)
            .isNotEmpty()
        setState { copy(notConnected = !connected) }
    }

    private fun save() {
        val state = state.value
        val start = state.selectedStart ?: return
        val dest = state.selectedDest ?: return
        if (!state.canSave) return
        setEffect { WidgetConfigEffect.Complete(startStation = start, destinationStation = dest) }
    }
}
