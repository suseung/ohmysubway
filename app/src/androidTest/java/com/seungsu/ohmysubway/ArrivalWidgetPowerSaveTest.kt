package com.seungsu.ohmysubway

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.seungsu.ohmysubway.widget.ArrivalWidgetReceiver
import com.seungsu.ohmysubway.widget.ArrivalWidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 절전모드에서 위젯 새로고침이 끊기거나 무한 로딩되던 문제의 회귀 테스트.
 *
 * 실 API를 호출하므로 네트워크가 필요하고, 사전에 아래 권한 부여가 필요하다.
 *   adb shell appwidget grantbind --package com.seungsu.ohmysubway
 */
@RunWith(AndroidJUnit4::class)
class ArrivalWidgetPowerSaveTest {

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

    /** 위젯 탭 처리 시간은 짧다. 조회가 그 안에 끝나지 않으면 프로세스가 잘려 loading 이 남는다. */
    @Test
    fun 새로고침은_예산_안에_끝난다() = runBlocking {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)

        val elapsedList = mutableListOf<Long>()
        repeat(REPEAT) { round ->
            // configure 는 상태를 새로 쓰므로 30초 캐시(isFresh)에 걸리지 않고 매번 실제로 조회한다
            val startedAt = System.currentTimeMillis()
            ArrivalWidgetUpdater.configure(context, glanceId, "영등포구청", "합정")
            val elapsed = System.currentTimeMillis() - startedAt
            elapsedList += elapsed

            val data = ArrivalWidgetUpdater.readData(context, glanceId)
            Log.i(TAG, "round=$round elapsed=${elapsed}ms error=${data.errorMessage} arrivals=${data.arrivals.size}")
            assertFalse("조회가 끝났으면 loading 이 남으면 안 된다", data.loading)
            delay(DATA_REFRESH_GAP_MILLIS)
        }

        val worst = elapsedList.max()
        Log.i(TAG, "elapsed=$elapsedList worst=${worst}ms budget=${BUDGET_MILLIS}ms")
        assertTrue(
            "새로고침이 예산을 넘겼다: ${worst}ms > ${BUDGET_MILLIS}ms (기록: $elapsedList)",
            worst <= BUDGET_MILLIS,
        )
    }

    /**
     * 절전모드에서 프로세스가 잘리는 상황을 코루틴 취소로 흉내 낸다.
     * 취소돼도 결과가 저장돼야 하고, loading 이 남으면 위젯이 영원히 "불러오는 중" 이 된다.
     */
    @Test
    fun 조회가_중간에_잘려도_loading_이_남지_않는다() = runBlocking {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)

        // configure 는 설정을 쓰고 곧바로 조회에 들어간다.
        val job = launch(Dispatchers.IO) {
            ArrivalWidgetUpdater.configure(context, glanceId, "영등포구청", "합정")
        }

        // 조회가 실제로 시작된(loading 이 켜진) 것을 확인한 뒤에 자른다.
        // 그냥 시간으로 자르면 조회 시작 전이나 끝난 뒤에 잘려 검증이 무의미해진다.
        var sawLoading = false
        val deadline = System.currentTimeMillis() + LOADING_WAIT_MILLIS
        while (System.currentTimeMillis() < deadline) {
            if (ArrivalWidgetUpdater.readData(context, glanceId).loading) {
                sawLoading = true
                break
            }
            delay(LOADING_POLL_MILLIS)
        }
        assertTrue("조회 중(loading) 상태를 한 번도 보지 못해 취소 경로를 검증할 수 없다", sawLoading)

        job.cancel()
        job.join()

        val data = ArrivalWidgetUpdater.readData(context, glanceId)
        Log.i(
            TAG,
            "취소 후 loading=${data.loading} loadingStartedAt=${data.loadingStartedAtMillis} " +
                "error=${data.errorMessage} updatedAt=${data.updatedAtMillis}",
        )
        assertFalse("취소된 뒤에도 loading 이 남아 있다 — 위젯이 무한 로딩된다", data.loading)
        assertFalse(
            "loading 표시가 풀리지 않는다",
            data.isRefreshing(System.currentTimeMillis()),
        )
    }

    companion object {
        private const val TAG = "PowerSaveTest"
        private const val HOST_ID = 0x7E57
        private const val REPEAT = 3

        /** 일일 호출 제한(1,000회)을 아끼려고 회차 사이에 잠깐 쉰다 */
        private const val DATA_REFRESH_GAP_MILLIS = 2_000L
        /** loading 이 켜지기를 기다리는 한도와 확인 주기 */
        private const val LOADING_WAIT_MILLIS = 5_000L
        private const val LOADING_POLL_MILLIS = 5L

        /** 위젯 탭 처리 시간 안에 끝나야 하는 한계 */
        private const val BUDGET_MILLIS = 8_000L
    }
}
