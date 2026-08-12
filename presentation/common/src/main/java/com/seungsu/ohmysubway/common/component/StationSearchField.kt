package com.seungsu.ohmysubway.common.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seungsu.ohmysubway.design.compose.theme.Grey60
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme
import com.seungsu.ohmysubway.domain.model.StationSummary

/** 역 이름 검색 입력 + 검색 결과 목록. 결과를 탭하면 선택된다. */
@Composable
fun StationSearchField(
    label: String,
    query: String,
    results: List<StationSummary>,
    onQueryChange: (String) -> Unit,
    onSelect: (StationSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        results.take(MAX_VISIBLE_RESULTS).forEach { station ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(station) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = station.name, style = OhMySubwayTheme.typos.bold.font14)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = station.lineNames.joinToString(" · "),
                    style = OhMySubwayTheme.typos.regular.font12,
                    color = Grey60,
                )
            }
        }
    }
}

private const val MAX_VISIBLE_RESULTS = 5
