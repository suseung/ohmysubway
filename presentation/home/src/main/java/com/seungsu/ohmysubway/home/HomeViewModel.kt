package com.seungsu.ohmysubway.home

import dagger.hilt.android.lifecycle.HiltViewModel
import com.seungsu.ohmysubway.core.mvi.MVIViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    // private val useCase: SomeUseCase
) : MVIViewModel<HomeIntent, HomeState, HomeEffect>() {

    override fun createInitialState() = HomeState()

    override suspend fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadData -> loadData()
            is HomeIntent.OnItemClick -> handleItemClick(intent.id)
        }
    }

    private fun loadData() {
        setLoadingEffect { true }
        launch {
            // TODO: useCase 호출
            setState { copy(isLoading = false, items = listOf("Item 1", "Item 2")) }
            setLoadingEffect { false }
        }
    }

    private fun handleItemClick(id: String) {
        setEffect { HomeEffect.NavigateTo("detail/$id") }
    }
}
