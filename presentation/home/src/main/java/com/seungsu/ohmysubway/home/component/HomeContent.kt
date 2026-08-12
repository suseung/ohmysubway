package com.seungsu.ohmysubway.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seungsu.ohmysubway.common.component.StationSearchField
import com.seungsu.ohmysubway.design.compose.ThemePreview
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme
import com.seungsu.ohmysubway.home.HomeIntent
import com.seungsu.ohmysubway.home.HomeState

@Composable
fun HomeContent(
    state: HomeState,
    uiAction: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = OhMySubwayTheme.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background.defaultBase)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(
            text = "오마이지하철",
            style = OhMySubwayTheme.typos.bold.font24,
            color = colors.label.onBgPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "시작역과 도착역을 고르면 도착역 방면 열차의 실시간 도착정보를 보여드려요.",
            style = OhMySubwayTheme.typos.regular.font14,
            color = colors.label.onBgSecondary,
        )
        Spacer(modifier = Modifier.height(24.dp))

        StationSearchField(
            label = "시작역",
            query = state.startQuery,
            results = state.startResults,
            onQueryChange = { uiAction(HomeIntent.UpdateStartQuery(it)) },
            onSelect = { uiAction(HomeIntent.SelectStart(it)) },
        )
        Spacer(modifier = Modifier.height(12.dp))
        StationSearchField(
            label = "도착역",
            query = state.destQuery,
            results = state.destResults,
            onQueryChange = { uiAction(HomeIntent.UpdateDestQuery(it)) },
            onSelect = { uiAction(HomeIntent.SelectDest(it)) },
        )

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { uiAction(HomeIntent.Lookup) },
            enabled = state.canLookup,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = if (state.isLoading) "조회 중…" else "도착정보 조회")
        }

        Spacer(modifier = Modifier.height(24.dp))
        when {
            state.isLoading -> CircularProgressIndicator()

            state.errorMessage != null -> Text(
                text = state.errorMessage.orEmpty(),
                style = OhMySubwayTheme.typos.regular.font14,
                color = colors.system.red,
            )

            state.lookedUp && state.arrivals.isEmpty() -> Text(
                text = "도착 예정 열차가 없어요.",
                style = OhMySubwayTheme.typos.regular.font14,
                color = colors.label.onBgSecondary,
            )

            else -> state.arrivals.forEach { arrival ->
                HomeItemRow(directedArrival = arrival)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@ThemePreview
@Composable
private fun HomeContentPreview() {
    OhMySubwayTheme {
        HomeContent(state = HomeState(startQuery = "강남", destQuery = "성수"), uiAction = {})
    }
}
