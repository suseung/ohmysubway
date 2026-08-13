package com.seungsu.ohmysubway.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.seungsu.ohmysubway.design.compose.ThemePreview
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme

@Composable
fun GuideScreen(onBackClick: () -> Unit) {
    val colors = OhMySubwayTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.defaultBase)
            .systemBarsPadding(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로",
                    tint = colors.label.onBgPrimary,
                )
            }
            Text(
                text = "도착시간 안내",
                style = OhMySubwayTheme.typos.bold.font18,
                color = colors.label.onBgPrimary,
            )
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            SectionTitle("표시된 시간은 얼마나 정확한가요?")
            Paragraph(
                "지하철 실시간 도착 정보는 약 24초(18~30초)마다 갱신되고, 앱이 받는 시점에는 이미 " +
                    "수십 초 지난 정보입니다. 이 앱은 그 지연을 빼서 보여주기 때문에 공식 앱보다 " +
                    "조금 더 이른 시간이 표시될 수 있습니다.",
            )
            Spacer(Modifier.height(8.dp))
            Paragraph(
                "보정을 하더라도 약 ±30초 오차는 남습니다. 열차가 역에 오래 서 있거나 " +
                    "운행이 늦어지면 API가 주는 예상 자체가 바뀌기 때문입니다.",
            )

            Spacer(Modifier.height(28.dp))
            SectionTitle("초 단위로 나오는 노선")
            Paragraph("아래 노선은 남은 시간이 초 단위로 제공되어, 지연 보정이 적용됩니다.")
            Spacer(Modifier.height(12.dp))
            LINES_WITH_SECONDS.forEach { LineRow(it) }

            Spacer(Modifier.height(28.dp))
            SectionTitle("몇 정거장 전만 나오는 노선")
            Paragraph(
                "아래 노선은 남은 시간을 아예 주지 않습니다. 그래서 \"3번째 전역\"처럼 " +
                    "위치만 표시되고, 시간 보정도 할 수 없습니다.",
            )
            Spacer(Modifier.height(12.dp))
            LINES_WITHOUT_SECONDS.forEach { LineRow(it) }

            Spacer(Modifier.height(28.dp))
            SectionTitle("위젯은 언제 갱신되나요?")
            Paragraph(
                "위젯을 누를 때만 새로 조회합니다. 배터리를 아끼려고 자동 갱신은 하지 않습니다. " +
                    "누른 뒤 30초 안에 다시 누르면 어차피 같은 데이터라 조회를 건너뜁니다.",
            )
            Spacer(Modifier.height(8.dp))
            Paragraph(
                "표시된 숫자는 홈 화면을 보고 있는 동안 스스로 줄어듭니다. 열차가 도착 시각을 " +
                    "지나면 0초로 바뀌지만, 잠깐 음수로 보일 수 있습니다. 그때 위젯을 누르면 최신 정보로 돌아옵니다.",
            )

            Spacer(Modifier.height(28.dp))
            Text(
                text = "측정 조건 — 2026년 8월 12일 18~19시, 역 8곳에서 도착정보 98건을 표본으로 " +
                    "측정했습니다. 시간대와 운행 상황에 따라 달라질 수 있습니다.",
                style = OhMySubwayTheme.typos.regular.font11,
                color = colors.label.onBgTertiary,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = OhMySubwayTheme.typos.bold.font16,
        color = OhMySubwayTheme.colors.label.onBgPrimary,
    )
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun Paragraph(text: String) {
    Text(
        text = text,
        style = OhMySubwayTheme.typos.regular.font14,
        color = OhMySubwayTheme.colors.label.onBgSecondary,
    )
}

@Composable
private fun LineRow(info: LineDelayInfo) {
    val colors = OhMySubwayTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.background.groupedBase)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = info.lineName,
                style = OhMySubwayTheme.typos.bold.font14,
                color = colors.label.onBgPrimary,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = info.medianDelayText,
                    style = OhMySubwayTheme.typos.bold.font16,
                    color = colors.system.blue,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "지연",
                    style = OhMySubwayTheme.typos.regular.font11,
                    color = colors.label.onBgTertiary,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (info.secondsProvidedPercent > 0) {
                "범위 ${info.rangeText} · 초 단위 제공 ${info.secondsProvidedPercent}%"
            } else {
                "범위 ${info.rangeText} · 초 단위 제공 없음"
            },
            style = OhMySubwayTheme.typos.regular.font12,
            color = colors.label.onBgSecondary,
        )
        info.note?.let {
            Spacer(Modifier.height(6.dp))
            Text(
                text = it,
                style = OhMySubwayTheme.typos.regular.font12,
                color = colors.system.red,
            )
        }
    }
}

@ThemePreview
@Composable
private fun GuideScreenPreview() {
    OhMySubwayTheme {
        GuideScreen(onBackClick = {})
    }
}
