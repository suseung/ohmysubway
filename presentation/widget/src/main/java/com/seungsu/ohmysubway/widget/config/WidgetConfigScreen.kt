package com.seungsu.ohmysubway.widget.config

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seungsu.ohmysubway.common.component.StationSearchField
import com.seungsu.ohmysubway.core.base.CollectEffect
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme
import com.seungsu.ohmysubway.design.compose.theme.Red50
import androidx.compose.foundation.layout.padding

@Composable
fun WidgetConfigScreen(
    viewModel: WidgetConfigViewModel,
    onComplete: (startStation: String, destinationStation: String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiAction = remember { viewModel::dispatch }

    CollectEffect(
        viewModel = viewModel,
        processEffect = { effect ->
            when (val configEffect = effect as WidgetConfigEffect) {
                is WidgetConfigEffect.Complete ->
                    onComplete(configEffect.startStation, configEffect.destinationStation)
            }
        },
    )

    WidgetConfigContent(state = state, uiAction = uiAction)
}

@Composable
private fun WidgetConfigContent(
    state: WidgetConfigState,
    uiAction: (WidgetConfigIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(text = "지하철 위젯 설정", style = OhMySubwayTheme.typos.bold.font24)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "시작역과 도착역을 고르면 도착역 방면 열차의\n실시간 도착정보를 위젯으로 보여드려요.",
            style = OhMySubwayTheme.typos.regular.font14,
        )
        Spacer(modifier = Modifier.height(24.dp))

        StationSearchField(
            label = "시작역",
            query = state.startQuery,
            results = state.startResults,
            onQueryChange = { uiAction(WidgetConfigIntent.UpdateStartQuery(it)) },
            onSelect = { uiAction(WidgetConfigIntent.SelectStart(it)) },
        )
        Spacer(modifier = Modifier.height(16.dp))

        StationSearchField(
            label = "도착역",
            query = state.destQuery,
            results = state.destResults,
            onQueryChange = { uiAction(WidgetConfigIntent.UpdateDestQuery(it)) },
            onSelect = { uiAction(WidgetConfigIntent.SelectDest(it)) },
        )

        if (state.notConnected) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "두 역이 같은 노선으로 연결되어 있지 않아요. 환승 없는 구간만 지원해요.",
                style = OhMySubwayTheme.typos.regular.font12,
                color = Red50,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { uiAction(WidgetConfigIntent.Save) },
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "위젯 추가")
        }
    }
}
