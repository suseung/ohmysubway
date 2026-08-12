package com.seungsu.ohmysubway.home

import com.seungsu.ohmysubway.core.mvi.ViewEffect
import com.seungsu.ohmysubway.core.mvi.ViewIntent
import com.seungsu.ohmysubway.core.mvi.ViewState

sealed interface HomeIntent : ViewIntent {
    data object LoadData : HomeIntent
    data class OnItemClick(val id: String) : HomeIntent
}

data class HomeState(
    val isLoading: Boolean = false,
    val items: List<String> = emptyList(),
    val error: String? = null
) : ViewState

sealed interface HomeEffect : ViewEffect {
    data class NavigateTo(val screen: Any) : HomeEffect
    data object ShowError : HomeEffect
}
