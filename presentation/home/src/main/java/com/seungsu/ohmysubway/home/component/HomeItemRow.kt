package com.seungsu.ohmysubway.home.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seungsu.ohmysubway.design.compose.ThemePreview
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme

@Composable
fun HomeItemRow(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = OhMySubwayTheme.typos.regular.font16,
        color = OhMySubwayTheme.colors.label.onBgPrimary,
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
    )
}

@ThemePreview
@Composable
private fun HomeItemRowPreview() {
    OhMySubwayTheme {
        HomeItemRow(text = "Sample Item")
    }
}
