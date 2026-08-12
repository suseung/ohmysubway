package com.seungsu.ohmysubway.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.action.clickable
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.seungsu.ohmysubway.domain.util.stationDisplayName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ArrivalAppWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val data = remember(prefs) { ArrivalWidgetData.decode(prefs[ArrivalWidgetData.PREF_KEY]) }
            GlanceTheme {
                WidgetContent(data)
            }
        }
    }
}

@Composable
private fun WidgetContent(data: ArrivalWidgetData) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(16.dp)
            .clickable(actionRunCallback<RefreshArrivalAction>())
            .padding(12.dp),
    ) {
        HeaderRow(data)
        Spacer(GlanceModifier.height(8.dp))
        when {
            !data.configured -> InfoText("위젯을 다시 추가해서 역을 설정해주세요")
            data.loading -> InfoText("불러오는 중…")
            data.errorMessage != null -> InfoText(data.errorMessage)
            data.arrivals.isEmpty() -> InfoText("도착 예정 열차가 없어요 · 눌러서 새로고침")
            else -> data.arrivals.forEach { item ->
                ArrivalRow(item)
                Spacer(GlanceModifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun HeaderRow(data: ArrivalWidgetData) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (data.configured) {
                "${data.startStation.stationDisplayName} → ${data.destinationStation.stationDisplayName}"
            } else {
                "지하철 도착정보"
            },
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = GlanceModifier.defaultWeight(),
        )
        if (data.updatedAtMillis > 0) {
            Text(
                text = formatTime(data.updatedAtMillis),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp),
            )
        }
    }
}

@Composable
private fun ArrivalRow(item: WidgetArrivalItem) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.lineName,
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = item.message,
            style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 13.sp),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
        )
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = "${item.terminalStation.stationDisplayName}행",
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp),
            maxLines = 1,
        )
    }
}

@Composable
private fun InfoText(message: String) {
    Text(
        text = message,
        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 13.sp),
    )
}

private fun formatTime(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale.KOREA).format(Date(millis))
