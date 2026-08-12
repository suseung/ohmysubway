package com.seungsu.ohmysubway.common.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
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
    val colors = OhMySubwayTheme.colors
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(label) },
            singleLine = true,
            textStyle = OhMySubwayTheme.typos.regular.font16,
            colors = OutlinedTextFieldDefaults.colors(
                // 창 배경은 항상 라이트인데 테마는 시스템 다크를 따라가므로,
                // 다크모드에서 흰 배경 + 흰 글씨가 되지 않도록 명시한다.
                focusedTextColor = colors.label.onBgPrimary,
                unfocusedTextColor = colors.label.onBgPrimary,
                cursorColor = colors.label.onBgPrimary,
                focusedLabelColor = colors.system.blue,
                unfocusedLabelColor = colors.label.onBgSecondary,
                focusedBorderColor = colors.system.blue,
                unfocusedBorderColor = colors.separator,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        results.take(MAX_VISIBLE_RESULTS).forEach { station ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // 역을 고르면 키보드를 내려 하단 버튼이 가려지지 않게 한다
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onSelect(station)
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = station.name,
                    style = OhMySubwayTheme.typos.bold.font14,
                    color = colors.label.onBgPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = station.lineNames.joinToString(" · "),
                    style = OhMySubwayTheme.typos.regular.font12,
                    color = colors.label.onBgSecondary,
                )
            }
        }
    }
}

private const val MAX_VISIBLE_RESULTS = 5
