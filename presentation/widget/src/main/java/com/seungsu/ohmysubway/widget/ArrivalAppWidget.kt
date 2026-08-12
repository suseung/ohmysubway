package com.seungsu.ohmysubway.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.seungsu.ohmysubway.domain.util.stationDisplayName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ArrivalAppWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    /** 위젯 크기에 맞춰 표시량을 조절하기 위해 실제 크기를 받는다. */
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val data = remember(prefs) { ArrivalWidgetData.decode(prefs[ArrivalWidgetData.PREF_KEY]) }
            WidgetContent(data)
        }
    }
}

@Composable
private fun WidgetContent(data: ArrivalWidgetData) {
    val size = LocalSize.current
    val colors = data.appearance.resolveColors()
    val compact = size.height < COMPACT_HEIGHT
    val showTerminal = size.width >= TERMINAL_MIN_WIDTH
    val showUpdatedAt = size.width >= TIMESTAMP_MIN_WIDTH && !compact
    val maxRows = if (compact) {
        1
    } else {
        ((size.height.value - HEADER_HEIGHT_DP) / ROW_HEIGHT_DP).toInt().coerceIn(1, MAX_WIDGET_ROWS)
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(colors.background)
            .cornerRadius(16.dp)
            .clickable(actionRunCallback<RefreshArrivalAction>())
            .padding(horizontal = 10.dp, vertical = if (compact) 6.dp else 10.dp),
    ) {
        HeaderRow(data = data, colors = colors, compact = compact, showUpdatedAt = showUpdatedAt)
        if (!compact) Spacer(GlanceModifier.height(6.dp))

        when {
            !data.configured -> InfoText("역을 설정해주세요", colors.secondaryText, compact)
            data.loading -> InfoText("불러오는 중…", colors.secondaryText, compact)
            data.errorMessage != null -> InfoText(data.errorMessage, colors.secondaryText, compact)
            data.arrivals.isEmpty() -> InfoText("도착 예정 열차 없음 · 눌러 새로고침", colors.secondaryText, compact)
            else -> data.arrivals.take(maxRows).forEachIndexed { index, item ->
                if (index > 0) Spacer(GlanceModifier.height(2.dp))
                ArrivalRow(item = item, colors = colors, compact = compact, showTerminal = showTerminal)
            }
        }
    }
}

@Composable
private fun HeaderRow(
    data: ArrivalWidgetData,
    colors: ResolvedWidgetColors,
    compact: Boolean,
    showUpdatedAt: Boolean,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (data.configured) {
                "${data.startStation.stationDisplayName}→${data.destinationStation.stationDisplayName}"
            } else {
                "지하철 도착정보"
            },
            style = TextStyle(
                color = androidx.glance.unit.ColorProvider(colors.primaryText),
                fontSize = if (compact) 12.sp else 14.sp,
                fontWeight = FontWeight.Bold,
            ),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
        )
        if (showUpdatedAt && data.updatedAtMillis > 0) {
            Text(
                text = formatTime(data.updatedAtMillis),
                style = TextStyle(
                    color = androidx.glance.unit.ColorProvider(colors.secondaryText),
                    fontSize = 10.sp,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ArrivalRow(
    item: WidgetArrivalItem,
    colors: ResolvedWidgetColors,
    compact: Boolean,
    showTerminal: Boolean,
) {
    val fontSize = if (compact) 13.sp else 14.sp
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.lineName,
            style = TextStyle(
                color = androidx.glance.unit.ColorProvider(colors.accent),
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Bold,
            ),
            maxLines = 1,
        )
        Spacer(GlanceModifier.width(5.dp))
        Text(
            text = item.message,
            style = TextStyle(
                color = androidx.glance.unit.ColorProvider(colors.primaryText),
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
        )
        if (showTerminal) {
            Spacer(GlanceModifier.width(5.dp))
            Text(
                text = "${item.terminalStation.stationDisplayName}행",
                style = TextStyle(
                    color = androidx.glance.unit.ColorProvider(colors.secondaryText),
                    fontSize = 10.sp,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun InfoText(message: String, color: androidx.compose.ui.graphics.Color, compact: Boolean) {
    Text(
        text = message,
        style = TextStyle(
            color = androidx.glance.unit.ColorProvider(color),
            fontSize = if (compact) 11.sp else 13.sp,
        ),
        maxLines = if (compact) 1 else 2,
    )
}

private fun formatTime(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale.KOREA).format(Date(millis))

private const val MAX_WIDGET_ROWS = 4
private const val HEADER_HEIGHT_DP = 30f
private const val ROW_HEIGHT_DP = 20f
private val COMPACT_HEIGHT = 80.dp
private val TERMINAL_MIN_WIDTH = 210.dp
private val TIMESTAMP_MIN_WIDTH = 150.dp
