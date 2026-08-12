package com.seungsu.ohmysubway.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
        processEffect = { },
    )

    HomeContent(state = state, uiAction = uiAction)
}
