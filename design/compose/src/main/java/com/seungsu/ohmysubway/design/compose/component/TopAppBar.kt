package com.seungsu.ohmysubway.design.compose.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seungsu.ohmysubway.design.compose.ThemePreview
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OhMySubwayTopAppBar(modifier: Modifier = Modifier, thickness: Dp = 0.5.dp, titleString: String = "", colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(), navigationIcon: @Composable () -> Unit = {}, actions: @Composable RowScope.() -> Unit = {}) {
    Column {
        TopAppBar(modifier = modifier, title = { Text(titleString, style = OhMySubwayTheme.typos.regular.font18, color = OhMySubwayTheme.colors.label.onBgPrimary) }, navigationIcon = navigationIcon, actions = actions, colors = colors)
        if (thickness != 0.dp) HorizontalDivider(color = OhMySubwayTheme.colors.label.onBgSecondary, thickness = thickness)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OhMySubwayTopAppBar(modifier: Modifier = Modifier, thickness: Dp = 0.5.dp, colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(), title: @Composable () -> Unit, navigationIcon: @Composable () -> Unit = {}, actions: @Composable RowScope.() -> Unit = {}) {
    Column {
        TopAppBar(modifier = modifier, title = title, navigationIcon = navigationIcon, actions = actions, colors = colors)
        if (thickness != 0.dp) HorizontalDivider(color = OhMySubwayTheme.colors.label.onBgSecondary, thickness = thickness)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OhMySubwayTopCenterAppBar(modifier: Modifier = Modifier, thickness: Dp = 0.5.dp, titleString: String = "", colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(), navigationIcon: @Composable () -> Unit = {}, actions: @Composable RowScope.() -> Unit = {}) {
    Column {
        CenterAlignedTopAppBar(modifier = modifier, title = { Text(titleString, style = OhMySubwayTheme.typos.bold.font18, color = OhMySubwayTheme.colors.label.onBgPrimary) }, navigationIcon = navigationIcon, actions = actions, colors = colors)
        if (thickness != 0.dp) HorizontalDivider(color = OhMySubwayTheme.colors.label.onBgSecondary, thickness = thickness)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreview
@Composable
private fun OhMySubwayTopAppBarPreview() {
    OhMySubwayTheme {
        OhMySubwayTopAppBar(titleString = "Title")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreview
@Composable
private fun OhMySubwayTopCenterAppBarPreview() {
    OhMySubwayTheme {
        OhMySubwayTopCenterAppBar(titleString = "Center Title")
    }
}
