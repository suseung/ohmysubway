package com.seungsu.ohmysubway.home

import com.seungsu.ohmysubway.core.mvi.ViewEffect
import com.seungsu.ohmysubway.core.mvi.ViewIntent
import com.seungsu.ohmysubway.core.mvi.ViewState
import com.seungsu.ohmysubway.domain.model.DirectedArrival
import com.seungsu.ohmysubway.domain.model.StationSummary

sealed interface HomeIntent : ViewIntent {
    data class UpdateStartQuery(val query: String) : HomeIntent
    data class UpdateDestQuery(val query: String) : HomeIntent
    data class SelectStart(val station: StationSummary) : HomeIntent
    data class SelectDest(val station: StationSummary) : HomeIntent
    data object Lookup : HomeIntent
}

data class HomeState(
    val startQuery: String = "",
    val destQuery: String = "",
    val startResults: List<StationSummary> = emptyList(),
    val destResults: List<StationSummary> = emptyList(),
    val selectedStart: String? = null,
    val selectedDest: String? = null,
    val isLoading: Boolean = false,
    val lookedUp: Boolean = false,
    val arrivals: List<DirectedArrival> = emptyList(),
    val errorMessage: String? = null,
) : ViewState {
    val canLookup: Boolean
        get() = selectedStart != null && selectedDest != null && selectedStart != selectedDest && !isLoading
}

sealed interface HomeEffect : ViewEffect {
    data object None : HomeEffect
}
