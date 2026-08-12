package com.seungsu.ohmysubway.widget.config

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seungsu.ohmysubway.common.component.StationSearchField
import com.seungsu.ohmysubway.core.base.CollectEffect
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme
import com.seungsu.ohmysubway.widget.MIN_ALPHA
import com.seungsu.ohmysubway.widget.WIDGET_BACKGROUND_PRESETS
import com.seungsu.ohmysubway.widget.WidgetAppearance
import com.seungsu.ohmysubway.widget.resolveColors

@Composable
fun WidgetConfigScreen(
    viewModel: WidgetConfigViewModel,
    onComplete: (startStation: String, destinationStation: String, appearance: WidgetAppearance) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiAction = remember { viewModel::dispatch }

    CollectEffect(
        viewModel = viewModel,
        processEffect = { effect ->
            when (val configEffect = effect as WidgetConfigEffect) {
                is WidgetConfigEffect.Complete -> onComplete(
                    configEffect.startStation,
                    configEffect.destinationStation,
                    configEffect.appearance,
                )
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
    val colors = OhMySubwayTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.defaultBase)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(
            text = "지하철 위젯 설정",
            style = OhMySubwayTheme.typos.bold.font24,
            color = colors.label.onBgPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "시작역과 도착역을 고르면 도착역 방면 열차의\n실시간 도착정보를 위젯으로 보여드려요.",
            style = OhMySubwayTheme.typos.regular.font14,
            color = colors.label.onBgSecondary,
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
                color = colors.system.red,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
        AppearanceSection(state = state, uiAction = uiAction)

        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = { uiAction(WidgetConfigIntent.Save) },
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "위젯 추가")
        }
    }
}

@Composable
private fun AppearanceSection(
    state: WidgetConfigState,
    uiAction: (WidgetConfigIntent) -> Unit,
) {
    val colors = OhMySubwayTheme.colors
    Text(
        text = "위젯 색상",
        style = OhMySubwayTheme.typos.bold.font16,
        color = colors.label.onBgPrimary,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "글자색은 배경에 맞춰 잘 보이는 색으로 자동 적용돼요.",
        style = OhMySubwayTheme.typos.regular.font12,
        color = colors.label.onBgSecondary,
    )
    Spacer(modifier = Modifier.height(12.dp))

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        WIDGET_BACKGROUND_PRESETS.forEach { (name, argb) ->
            val selected = state.appearance.backgroundArgb == argb
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                // 색 이름을 눌러도 선택되도록 항목 전체를 클릭 영역으로 두고,
                // 선택 상태를 접근성에 노출한다.
                modifier = Modifier
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { uiAction(WidgetConfigIntent.UpdateBackground(argb)) },
                    )
                    .semantics(mergeDescendants = true) { contentDescription = "배경색 $name" },
            ) {
                Spacer(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(argb))
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) colors.system.blue else colors.separator,
                            shape = CircleShape,
                        ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = name,
                    style = OhMySubwayTheme.typos.regular.font11,
                    color = if (selected) colors.system.blue else colors.label.onBgSecondary,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "투명도",
            style = OhMySubwayTheme.typos.regular.font14,
            color = colors.label.onBgPrimary,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${(state.appearance.backgroundAlpha * 100).toInt()}%",
            style = OhMySubwayTheme.typos.bold.font14,
            color = colors.label.onBgSecondary,
        )
    }
    Slider(
        value = state.appearance.backgroundAlpha,
        onValueChange = { uiAction(WidgetConfigIntent.UpdateBackgroundAlpha(it)) },
        valueRange = MIN_ALPHA..1f,
    )

    Spacer(modifier = Modifier.height(8.dp))
    WidgetPreview(state = state)
}

/** 고른 색이 실제 위젯에서 어떻게 보이는지 미리 보여준다. */
@Composable
private fun WidgetPreview(state: WidgetConfigState) {
    val resolved = state.appearance.resolveColors()
    Text(
        text = "미리보기",
        style = OhMySubwayTheme.typos.regular.font12,
        color = OhMySubwayTheme.colors.label.onBgSecondary,
    )
    Spacer(modifier = Modifier.height(6.dp))
    // 투명도가 체감되도록 배경화면 대신 격자 느낌의 받침을 깔아준다
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PREVIEW_BACKDROP)
            .padding(10.dp),
    ) {
        WidgetPreviewCard(state = state, resolved = resolved)
    }
}

@Composable
private fun WidgetPreviewCard(
    state: WidgetConfigState,
    resolved: com.seungsu.ohmysubway.widget.ResolvedWidgetColors,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(resolved.background)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(
            text = "${state.selectedStart ?: "시작역"}→${state.selectedDest ?: "도착역"}",
            style = OhMySubwayTheme.typos.bold.font14,
            color = resolved.primaryText,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "2호선",
                style = OhMySubwayTheme.typos.bold.font12,
                color = resolved.accent,
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "3분 20초 후",
                style = OhMySubwayTheme.typos.regular.font14,
                color = resolved.primaryText,
            )
        }
    }
}

/** 미리보기에서 투명도가 드러나게 하는 받침 색. */
private val PREVIEW_BACKDROP = Color(0xFF6B7A8F)
