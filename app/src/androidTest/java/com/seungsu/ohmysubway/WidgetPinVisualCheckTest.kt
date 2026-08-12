package com.seungsu.ohmysubway

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.seungsu.ohmysubway.widget.ArrivalWidgetReceiver
import com.seungsu.ohmysubway.widget.ArrivalWidgetUpdater
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 위젯을 홈 화면에 고정하고 설정까지 적용한다 — 위젯 실제 렌더링을 눈으로 확인하기 위한 보조 테스트.
 * 런처의 "홈 화면에 추가" 확인 창을 UiAutomator로 수락한다.
 */
@RunWith(AndroidJUnit4::class)
class WidgetPinVisualCheckTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun 위젯을_홈에_고정하고_강남에서_성수방면으로_설정한다() = runBlocking {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val manager = GlanceAppWidgetManager(context)

        device.pressHome()
        manager.requestPinGlanceAppWidget(ArrivalWidgetReceiver::class.java)

        // 런처 확인 창 수락 (문구는 런처/언어에 따라 다르다)
        device.wait(Until.hasObject(By.textContains("Add")), CONFIRM_TIMEOUT_MS)
        listOf("Add automatically", "Add to home screen", "Add", "추가").firstNotNullOfOrNull { label ->
            device.findObject(By.text(label))?.also { it.click() }
        }

        val glanceId = waitForGlanceId(manager)
        assertTrue("위젯이 홈에 고정되지 않았다", glanceId != null)

        ArrivalWidgetUpdater.configure(
            context = context,
            glanceId = glanceId!!,
            startStation = "강남",
            destinationStation = "성수",
        )

        device.pressHome()
        delay(RENDER_SETTLE_MS)
    }

    private suspend fun waitForGlanceId(manager: GlanceAppWidgetManager) =
        (1..PIN_POLL_ATTEMPTS).firstNotNullOfOrNull {
            manager.getGlanceIds(com.seungsu.ohmysubway.widget.ArrivalAppWidget::class.java)
                .firstOrNull()
                ?: run { delay(PIN_POLL_INTERVAL_MS); null }
        }

    companion object {
        private const val CONFIRM_TIMEOUT_MS = 5_000L
        private const val PIN_POLL_ATTEMPTS = 20
        private const val PIN_POLL_INTERVAL_MS = 500L
        private const val RENDER_SETTLE_MS = 2_000L
    }
}
