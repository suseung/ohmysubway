package com.seungsu.ohmysubway.home.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seungsu.ohmysubway.design.compose.ThemePreview
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme
import com.seungsu.ohmysubway.home.HomeIntent
import com.seungsu.ohmysubway.home.HomeState

@Composable
fun HomeContent(
    state: HomeState,
    uiAction: (HomeIntent) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        state.items.forEach { item ->
            HomeItemRow(text = item)
        }
    }
}

@ThemePreview
@Composable
private fun HomeContentPreview() {
    OhMySubwayTheme {
        HomeContent(
            state = HomeState(items = listOf("Item 1", "Item 2", "Item 3"))
        )
    }
}

@ThemePreview
@Composable
private fun HomeContentEmptyPreview() {
    OhMySubwayTheme {
        HomeContent(state = HomeState())
    }
}
