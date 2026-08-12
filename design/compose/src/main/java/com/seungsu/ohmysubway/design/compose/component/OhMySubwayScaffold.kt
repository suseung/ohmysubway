package com.seungsu.ohmysubway.design.compose.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OhMySubwayScaffold(
    modifier: Modifier = Modifier,
    topAppBarColor: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    containerColor: Color = OhMySubwayTheme.colors.background.defaultBase,
    navigationIcon: @Composable () -> Unit = {},
    titleString: String = "",
    titleThickness: Dp = 0.5.dp,
    isTitleCenter: Boolean = true,
    contentColor: Color = contentColorFor(containerColor),
    applySystemBarsTopPadding: Boolean = true,
    applySystemBarsBottomPadding: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: (@Composable () -> Unit)? = null,
    contents: @Composable (Modifier) -> Unit = {}
) {
    val windowInsets = when {
        applySystemBarsTopPadding && applySystemBarsBottomPadding -> WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Vertical)
        applySystemBarsTopPadding -> WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
        applySystemBarsBottomPadding -> WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
        else -> WindowInsets(0)
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isTitleCenter) OhMySubwayTopCenterAppBar(navigationIcon = navigationIcon, titleString = titleString, actions = actions, thickness = titleThickness, colors = topAppBarColor)
            else OhMySubwayTopAppBar(navigationIcon = navigationIcon, titleString = titleString, actions = actions, thickness = titleThickness, colors = topAppBarColor)
        },
        bottomBar = {
            bottomBar?.let {
                Box(modifier = if (applySystemBarsBottomPadding) Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)) else Modifier) { it() }
            }
        },
        containerColor = containerColor, contentColor = contentColor, contentWindowInsets = windowInsets
    ) { paddingValues -> contents(Modifier.padding(paddingValues)) }
}
