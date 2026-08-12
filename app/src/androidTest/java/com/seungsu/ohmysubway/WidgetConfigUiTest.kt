package com.seungsu.ohmysubway

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.seungsu.ohmysubway.widget.ArrivalWidgetData
import com.seungsu.ohmysubway.widget.ArrivalWidgetReceiver
import com.seungsu.ohmysubway.widget.ArrivalWidgetUpdater
import com.seungsu.ohmysubway.widget.config.ArrivalWidgetConfigActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** 위젯 설정 화면(역 선택 + 배경색/투명도) UI 검증. */
@RunWith(AndroidJUnit4::class)
class WidgetConfigUiTest {

    private lateinit var device: UiDevice
    private lateinit var robot: StationPickerRobot
    private lateinit var host: AppWidgetHost
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        device = UiDevice.getInstance(instrumentation)
        robot = StationPickerRobot(device)
        val context = instrumentation.targetContext

        host = AppWidgetHost(context, HOST_ID)
        appWidgetId = host.allocateAppWidgetId()
        val bound = AppWidgetManager.getInstance(context).bindAppWidgetIdIfAllowed(
            appWidgetId,
            ComponentName(context, ArrivalWidgetReceiver::class.java),
        )
        assertTrue("위젯 바인딩 실패 — appwidget grantbind 필요", bound)

        val intent = Intent(context, ArrivalWidgetConfigActivity::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        assertTrue(
            "설정 화면이 떠야 한다",
            device.wait(Until.hasObject(By.text("지하철 위젯 설정")), TIMEOUT_MS),
        )
    }

    @After
    fun tearDown() {
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            host.deleteAppWidgetId(appWidgetId)
        }
    }

    @Test
    fun 역과_배경색을_고르면_위젯_추가_버튼이_활성화된다() {
        assertTrue("색상 설정 섹션이 있어야 한다", device.hasObject(By.text("위젯 색상")))

        val fields = robot.editFields()
        assertTrue("입력 필드가 2개 있어야 한다", fields.size >= 2)

        robot.selectStation(fields[0], "강남")
        robot.selectStation(fields[1], "성수")

        // 역을 고르면 키보드가 내려가 색상 설정까지 보여야 한다
        assertTrue(
            "투명도 조절이 보여야 한다",
            device.wait(Until.hasObject(By.text("투명도")), TIMEOUT_MS),
        )
        // 배경색을 바꾸면 선택 상태가 실제로 옮겨가야 한다
        val navy = device.wait(Until.findObject(By.desc("배경색 남색")), TIMEOUT_MS)
        assertNotNull("배경색 항목을 찾아야 한다", navy)
        navy.click()
        device.waitForIdle()
        robot.screenshot("widget_config.png")

        device.findObject(By.scrollable(true))?.scroll(Direction.DOWN, 1f)
        device.waitForIdle()
        val addButton = device.wait(Until.findObject(By.text("위젯 추가")), TIMEOUT_MS)
        assertNotNull("추가 버튼을 찾아야 한다", addButton)
        assertTrue("설정을 마치면 추가 버튼이 눌릴 수 있어야 한다", addButton.isEnabled)
        addButton.click()

        // 화면에서 고른 값이 실제 위젯 설정으로 저장돼야 한다
        val saved = awaitSavedData()
        assertEquals("강남", saved.startStation)
        assertEquals("성수", saved.destinationStation)
        assertEquals(NAVY_ARGB, saved.appearance.backgroundArgb)
    }

    /** 저장은 비동기(첫 조회 포함)라 설정이 반영될 때까지 잠깐 기다린다. */
    private fun awaitSavedData(): ArrivalWidgetData = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
        repeat(SAVE_POLL_ATTEMPTS) {
            val data = ArrivalWidgetUpdater.readData(context, glanceId)
            if (data.configured) return@runBlocking data
            delay(SAVE_POLL_INTERVAL_MS)
        }
        ArrivalWidgetData()
    }

    companion object {
        private const val HOST_ID = 2048
        private const val TIMEOUT_MS = 5_000L
        private const val SAVE_POLL_ATTEMPTS = 20
        private const val SAVE_POLL_INTERVAL_MS = 500L
        private const val NAVY_ARGB = 0xFF12294A.toInt()
    }
}
