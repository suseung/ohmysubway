package com.seungsu.ohmysubway.widget.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.suseung.designsystem.theme.SsTheme
import com.seungsu.ohmysubway.widget.ArrivalWidgetUpdater
import com.seungsu.ohmysubway.widget.WidgetAppearance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** 위젯을 홈 화면에 추가할 때(또는 다시 설정할 때) 뜨는 설정 화면. */
@AndroidEntryPoint
class ArrivalWidgetConfigActivity : ComponentActivity() {

    private val viewModel: WidgetConfigViewModel by viewModels()

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        // 35+ 는 앱이 상태바 뒤까지 그리도록 강제된다. 이걸 부르지 않으면 상태바 아이콘 색을
        // 시스템이 흰색으로 두어, 흰 배경에서는 시각·배터리가 아예 안 보이고 빈 띠처럼 보인다.
        enableEdgeToEdge()
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

        loadExistingSettings()

        setContent {
            SsTheme {
                WidgetConfigScreen(
                    viewModel = viewModel,
                    onComplete = ::completeConfiguration,
                )
            }
        }
    }

    /** 이미 배치된 위젯을 다시 설정하는 경우 기존 설정을 채워 넣는다. */
    private fun loadExistingSettings() {
        lifecycleScope.launch {
            val existing = runCatching {
                val glanceId = GlanceAppWidgetManager(this@ArrivalWidgetConfigActivity)
                    .getGlanceIdBy(appWidgetId)
                ArrivalWidgetUpdater.readData(applicationContext, glanceId)
            }.getOrNull() ?: return@launch

            viewModel.dispatch(
                WidgetConfigIntent.Load(
                    WidgetConfigInitialData(
                        startStation = existing.startStation,
                        destinationStation = existing.destinationStation,
                        appearance = existing.appearance,
                    ),
                ),
            )
        }
    }

    private fun completeConfiguration(
        startStation: String,
        destinationStation: String,
        appearance: WidgetAppearance,
    ) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(this@ArrivalWidgetConfigActivity)
                .getGlanceIdBy(appWidgetId)
            ArrivalWidgetUpdater.configure(
                context = applicationContext,
                glanceId = glanceId,
                startStation = startStation,
                destinationStation = destinationStation,
                appearance = appearance,
            )
            setResult(
                RESULT_OK,
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
            )
            finish()
        }
    }
}
