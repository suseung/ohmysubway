package com.seungsu.ohmysubway.widget.config

import com.seungsu.ohmysubway.core.mvi.ViewEffect
import com.seungsu.ohmysubway.core.mvi.ViewIntent
import com.seungsu.ohmysubway.core.mvi.ViewState
import com.seungsu.ohmysubway.domain.model.StationSummary
import com.seungsu.ohmysubway.widget.WidgetAppearance

sealed interface WidgetConfigIntent : ViewIntent {
    /** 이미 설정된 위젯을 다시 설정할 때 기존 값을 불러온다. */
    data class Load(val data: WidgetConfigInitialData) : WidgetConfigIntent
    data class UpdateStartQuery(val query: String) : WidgetConfigIntent
    data class UpdateDestQuery(val query: String) : WidgetConfigIntent
    data class SelectStart(val station: StationSummary) : WidgetConfigIntent
    data class SelectDest(val station: StationSummary) : WidgetConfigIntent
    data class UpdateBackground(val argb: Int) : WidgetConfigIntent
    data class UpdateBackgroundAlpha(val alpha: Float) : WidgetConfigIntent
    data object Save : WidgetConfigIntent
}

data class WidgetConfigInitialData(
    val startStation: String,
    val destinationStation: String,
    val appearance: WidgetAppearance,
)

data class WidgetConfigState(
    val startQuery: String = "",
    val destQuery: String = "",
    val startResults: List<StationSummary> = emptyList(),
    val destResults: List<StationSummary> = emptyList(),
    val selectedStart: String? = null,
    val selectedDest: String? = null,
    val notConnected: Boolean = false,
    val appearance: WidgetAppearance = WidgetAppearance(),
) : ViewState {
    val canSave: Boolean
        get() = selectedStart != null &&
            selectedDest != null &&
            selectedStart != selectedDest &&
            !notConnected
}

sealed interface WidgetConfigEffect : ViewEffect {
    data class Complete(
        val startStation: String,
        val destinationStation: String,
        val appearance: WidgetAppearance,
    ) : WidgetConfigEffect
}
