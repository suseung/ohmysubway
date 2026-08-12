package com.seungsu.ohmysubway.widget.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme
import com.seungsu.ohmysubway.widget.ArrivalWidgetUpdater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** 위젯을 홈 화면에 추가할 때 뜨는 설정 화면 — 시작역/도착역을 고른다. */
@AndroidEntryPoint
class ArrivalWidgetConfigActivity : ComponentActivity() {

    private val viewModel: WidgetConfigViewModel by viewModels()

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            OhMySubwayTheme {
                WidgetConfigScreen(
                    viewModel = viewModel,
                    onComplete = ::completeConfiguration,
                )
            }
        }
    }

    private fun completeConfiguration(startStation: String, destinationStation: String) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(this@ArrivalWidgetConfigActivity)
                .getGlanceIdBy(appWidgetId)
            ArrivalWidgetUpdater.configure(
                context = applicationContext,
                glanceId = glanceId,
                startStation = startStation,
                destinationStation = destinationStation,
            )
            setResult(
                RESULT_OK,
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
            )
            finish()
        }
    }
}
