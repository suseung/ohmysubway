package com.seungsu.ohmysubway.widget.config

import com.seungsu.ohmysubway.core.mvi.ViewEffect
import com.seungsu.ohmysubway.core.mvi.ViewIntent
import com.seungsu.ohmysubway.core.mvi.ViewState
import com.seungsu.ohmysubway.domain.model.StationSummary

sealed interface WidgetConfigIntent : ViewIntent {
    data class UpdateStartQuery(val query: String) : WidgetConfigIntent
    data class UpdateDestQuery(val query: String) : WidgetConfigIntent
    data class SelectStart(val station: StationSummary) : WidgetConfigIntent
    data class SelectDest(val station: StationSummary) : WidgetConfigIntent
    data object Save : WidgetConfigIntent
}

data class WidgetConfigState(
    val startQuery: String = "",
    val destQuery: String = "",
    val startResults: List<StationSummary> = emptyList(),
    val destResults: List<StationSummary> = emptyList(),
    val selectedStart: String? = null,
    val selectedDest: String? = null,
    val notConnected: Boolean = false,
) : ViewState {
    val canSave: Boolean
        get() = selectedStart != null &&
            selectedDest != null &&
            selectedStart != selectedDest &&
            !notConnected
}

sealed interface WidgetConfigEffect : ViewEffect {
    data class Complete(val startStation: String, val destinationStation: String) : WidgetConfigEffect
}
