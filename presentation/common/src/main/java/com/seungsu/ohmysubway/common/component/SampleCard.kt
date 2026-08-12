package com.seungsu.ohmysubway.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seungsu.ohmysubway.common.model.SampleUiModel
import com.seungsu.ohmysubway.design.compose.ThemePreview
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme

@Composable
fun SampleCard(
    item: SampleUiModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.title, style = OhMySubwayTheme.typos.bold.font16)
            if (!item.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.description, style = OhMySubwayTheme.typos.regular.font14)
            }
        }
    }
}

@ThemePreview
@Composable
private fun SampleCardPreview() {
    OhMySubwayTheme {
        SampleCard(
            item = SampleUiModel(
                id = "1",
                title = "Sample Title",
                description = "Sample description text",
                imageUrl = null
            )
        )
    }
}
