package com.seungsu.ohmysubway.home

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seungsu.ohmysubway.core.base.CollectEffect
import com.seungsu.ohmysubway.home.component.HomeContent

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiAction = remember { viewModel::dispatch }

    CollectEffect(
        viewModel = viewModel,
        processEffect = { effect ->
            when (effect as HomeEffect) {
                is HomeEffect.NavigateTo -> { /* TODO: 네비게이션 추가 시 처리 */ }
                HomeEffect.ShowError -> { /* 에러 처리 */ }
            }
        }
    )

    LaunchedEffect(Unit) {
        uiAction(HomeIntent.LoadData)
    }

    HomeContent(state = state, uiAction = uiAction)
}
