package com.seungsu.ohmysubway

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.seungsu.ohmysubway.widget.ArrivalWidgetData
import com.seungsu.ohmysubway.widget.ArrivalWidgetReceiver
import com.seungsu.ohmysubway.widget.ArrivalWidgetUpdater
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 실제 위젯을 바인딩해 설정 → 조회 → 상태 갱신 전 구간을 검증한다.
 * 실 API를 호출하므로 네트워크가 필요하고, 사전에 아래 권한 부여가 필요하다.
 *   adb shell appwidget grantbind --package com.seungsu.ohmysubway
 */
@RunWith(AndroidJUnit4::class)
class ArrivalWidgetE2ETest {

    private lateinit var context: Context
    private lateinit var host: AppWidgetHost
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        host = AppWidgetHost(context, HOST_ID)
        appWidgetId = host.allocateAppWidgetId()
        val bound = AppWidgetManager.getInstance(context).bindAppWidgetIdIfAllowed(
            appWidgetId,
            ComponentName(context, ArrivalWidgetReceiver::class.java),
        )
        assertTrue("위젯 바인딩 실패 — appwidget grantbind 필요", bound)
    }

    @After
    fun tearDown() {
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            host.deleteAppWidgetId(appWidgetId)
        }
    }

    @Test
    fun 강남에서_성수방면_설정하면_해당방향_도착정보가_저장된다() = runBlocking {
        val data = configureAndRead(startStation = "강남", destinationStation = "성수")

        assertEquals("강남", data.startStation)
        assertEquals("성수", data.destinationStation)
        assertNull("오류 없이 조회돼야 한다: ${data.errorMessage}", data.errorMessage)
        assertTrue("갱신 시각이 기록돼야 한다", data.updatedAtMillis > 0)

        // 강남 → 성수는 2호선 외선(역삼 방면). 운행 시간대라면 도착정보가 있어야 하고,
        // 있다면 모두 2호선이어야 한다 (신분당선 열차가 섞이면 방향 판별 실패).
        data.arrivals.forEach { arrival ->
            assertEquals("2호선", arrival.lineName)
            assertTrue("도착 메시지가 있어야 한다", arrival.message.isNotBlank())
        }
    }

    @Test
    fun 연결되지_않은_두_역이면_안내메시지가_저장된다() = runBlocking {
        val data = configureAndRead(startStation = "강남", destinationStation = "불암산")

        assertTrue("연결 안 됨 안내가 있어야 한다", data.errorMessage?.contains("연결") == true)
        assertTrue("도착정보는 비어야 한다", data.arrivals.isEmpty())
    }

    private suspend fun configureAndRead(
        startStation: String,
        destinationStation: String,
    ): ArrivalWidgetData {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
        ArrivalWidgetUpdater.configure(
            context = context,
            glanceId = glanceId,
            startStation = startStation,
            destinationStation = destinationStation,
        )
        return ArrivalWidgetUpdater.readData(context, glanceId)
    }

    companion object {
        private const val HOST_ID = 1024
    }
}
