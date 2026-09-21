package com.seungsu.ohmysubway.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.seungsu.ohmysubway.domain.model.DirectedArrivals
import com.seungsu.ohmysubway.domain.usecase.GetDirectedArrivalsUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** 위젯 상태 저장/새로고침 담당. Glance 콜백에서는 Hilt 주입이 안 되므로 EntryPoint로 접근한다. */
object ArrivalWidgetUpdater {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun getDirectedArrivalsUseCase(): GetDirectedArrivalsUseCase
    }

    private const val MAX_WIDGET_ARRIVALS = 4

    /**
     * 같은 위젯 상태 파일에 동시에 접근하면 Glance가 DataStore 중복 오류를 던지므로
     * (위젯을 빠르게 두 번 누르는 경우 등) 상태 읽기/쓰기를 이 뮤텍스로 직렬화한다.
     */
    private val stateMutex = Mutex()

    /** 저장된 위젯 설정을 읽는다. */
    suspend fun readData(context: Context, glanceId: GlanceId): ArrivalWidgetData =
        stateMutex.withLock { readDataLocked(context, glanceId) }

    /**
     * 모든 위젯을 다시 그린다 (조회 없이 화면만).
     * 카운트다운이 0에 가까워졌을 때 표시를 정리하기 위해 알람에서 호출된다.
     */
    suspend fun rerenderAll(context: Context) {
        val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(ArrivalAppWidget::class.java)
        glanceIds.forEach { ArrivalAppWidget().update(context, it) }
        scheduleNextRerender(context, glanceIds)
    }

    /**
     * 가장 이른 도착 시각에 위젯을 다시 그리도록 예약한다.
     * Chronometer는 값으로 멈추지 못하므로, 이때 다시 그려서 "0초"로 바꿔준다.
     *
     * 부정확 알람(RTC)이라 별도 권한이 필요 없지만 안드로이드가 미룰 수 있다.
     * 늦어지면 도착 시각 이후 잠깐 음수가 보일 수 있고, 위젯을 누르면 정상으로 돌아온다.
     */
    private suspend fun scheduleNextRerender(context: Context, glanceIds: List<GlanceId>) {
        val now = System.currentTimeMillis()
        val nextBoundary = glanceIds
            .map { readDataLocked(context, it) }
            .flatMap { data -> data.arrivals.mapNotNull { it.arrivalAtMillis } }
            .map { it + STOP_GRACE_MILLIS }
            .filter { it > now }
            .minOrNull()

        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            RERENDER_REQUEST_CODE,
            Intent(context, ArrivalWidgetRerenderReceiver::class.java)
                .setAction(ArrivalWidgetRerenderReceiver.ACTION_RERENDER),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        if (nextBoundary == null) {
            alarmManager.cancel(pendingIntent)
            return
        }
        alarmManager.set(AlarmManager.RTC, nextBoundary, pendingIntent)
    }

    /** 위젯 설정을 저장하고 첫 조회까지 수행한다. */
    suspend fun configure(
        context: Context,
        glanceId: GlanceId,
        startStation: String,
        destinationStation: String,
        appearance: WidgetAppearance = WidgetAppearance(),
    ) {
        stateMutex.withLock {
            writeDataLocked(
                context, glanceId,
                ArrivalWidgetData(
                    startStation = startStation,
                    destinationStation = destinationStation,
                    appearance = appearance,
                ),
            )
        }
        refresh(context, glanceId)
    }

    /**
     * 저장된 설정으로 도착정보를 다시 조회해 위젯을 갱신한다.
     *
     * 절전모드 대응: 위젯 탭은 브로드캐스트로 들어오고, 그 처리 시간이 지나면 시스템이
     * 프로세스를 얼리거나 죽인다. 30초 타임아웃으로 기다리면 응답을 받기 전에 잘려
     * loading이 남은 채로 "불러오는 중"만 계속 보이게 된다. 그래서
     * 1) 조회 전체에 예산(REFRESH_BUDGET_MILLIS)을 걸고,
     * 2) 예산 안에서 한 번 더 시도하고,
     * 3) 어떤 경우에도(취소 포함) 마지막에 결과를 반드시 저장한다.
     */
    suspend fun refresh(context: Context, glanceId: GlanceId) {
        val current = stateMutex.withLock {
            val stored = readDataLocked(context, glanceId)
            val now = System.currentTimeMillis()
            if (!stored.configured) {
                // 설정이 없는(앱 재설치 등으로 상태를 잃은) 위젯도 안내 문구는 그려준다
                ArrivalAppWidget().update(context, glanceId)
                return
            }
            if (stored.isRefreshing(now)) {
                // 이미 조회 중. 화면만 다시 그려 남아 있던 표시를 정리한다
                ArrivalAppWidget().update(context, glanceId)
                return
            }
            if (stored.isFresh(now)) {
                // 30초 안에 다시 눌렀으면 같은 데이터라 호출을 생략하고 화면만 다시 그린다
                ArrivalAppWidget().update(context, glanceId)
                return
            }

            stored.also {
                writeDataLocked(context, glanceId, it.copy(loading = true, loadingStartedAtMillis = now))
            }
        }

        val refreshed = try {
            withTimeoutOrNull(ArrivalWidgetData.REFRESH_BUDGET_MILLIS) {
                fetchWithRetry(context, current)
            } ?: current.copy(loading = false, errorMessage = SLOW_NETWORK_MESSAGE)
        } catch (e: CancellationException) {
            // 브로드캐스트 처리 시간이 끝나 잘린 경우. loading을 남기지 않고 넘긴다.
            persist(context, glanceId, current.copy(loading = false, errorMessage = SLOW_NETWORK_MESSAGE))
            throw e
        } catch (e: Exception) {
            current.copy(loading = false, errorMessage = FAILED_MESSAGE)
        }

        persist(context, glanceId, refreshed)
    }

    /**
     * 예산이 남아 있는 한 한 번 더 시도한다.
     * 절전모드에서 깨어난 직후에는 통신 모듈이 아직 붙지 않아 첫 시도가 곧바로 실패하는 일이 많다.
     */
    private suspend fun fetchWithRetry(context: Context, current: ArrivalWidgetData): ArrivalWidgetData {
        val useCase = EntryPointAccessors
            .fromApplication(context, WidgetEntryPoint::class.java)
            .getDirectedArrivalsUseCase()
        val params = GetDirectedArrivalsUseCase.Params(
            startStation = current.startStation,
            destinationStation = current.destinationStation,
        )

        var lastFailure: ArrivalWidgetData? = null
        repeat(FETCH_ATTEMPTS) { attempt ->
            val result = runCatching { useCase(params) }
            result.fold(
                onSuccess = { directed ->
                    return when (directed) {
                        is DirectedArrivals.NotConnected -> current.copy(
                            loading = false,
                            errorMessage = "두 역이 같은 노선으로 연결되어 있지 않아요",
                        )

                        is DirectedArrivals.Success -> {
                            val fetchedAt = System.currentTimeMillis()
                            current.copy(
                                loading = false,
                                errorMessage = null,
                                updatedAtMillis = fetchedAt,
                                arrivals = directed.arrivals.take(MAX_WIDGET_ARRIVALS).map { item ->
                                    // 데이터 지연을 보정한 남은 시간으로 도착 예정 시각을 만든다
                                    val remaining = item.arrival.remainingSeconds(fetchedAt)
                                    WidgetArrivalItem(
                                        lineName = item.lineName,
                                        message = item.arrival.arrivalMessage,
                                        terminalStation = item.arrival.terminalStation,
                                        arrivalAtMillis = remaining?.let { fetchedAt + it * 1000L },
                                    )
                                },
                            )
                        }
                    }
                },
                onFailure = { error ->
                    if (error is CancellationException) throw error
                    logFailure(context, error)
                    lastFailure = current.copy(loading = false, errorMessage = FAILED_MESSAGE)
                    if (attempt < FETCH_ATTEMPTS - 1) delay(RETRY_DELAY_MILLIS)
                },
            )
        }
        return lastFailure ?: current.copy(loading = false, errorMessage = FAILED_MESSAGE)
    }

    /**
     * 결과를 반드시 저장한다. 취소된 뒤에 불려도 저장이 끊기지 않도록 NonCancellable로 감싼다.
     * 이걸 놓치면 loading이 남아 위젯이 계속 "불러오는 중"으로 보인다.
     */
    private suspend fun persist(context: Context, glanceId: GlanceId, data: ArrivalWidgetData) {
        withContext(NonCancellable) {
            runCatching {
                stateMutex.withLock { writeDataLocked(context, glanceId, data) }
            }
        }
    }

    /**
     * 실패 원인을 로그로 남긴다.
     *
     * 절전모드에서는 앱의 네트워크가 정책으로 막히면 activeNetwork 가 null 로 온다.
     * 데이터를 꺼둔 것과 구분이 되지 않으므로 이걸로 조회를 막지는 않는다 —
     * 위젯 탭은 잠깐 예외를 받기 때문에 막힌 것처럼 보여도 요청이 성공할 수 있다.
     */
    private fun logFailure(context: Context, error: Throwable) {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        val active = runCatching { manager?.activeNetwork }.getOrNull()
        Log.w(TAG, "새로고침 실패 (activeNetwork=${if (active == null) "null" else "있음"})", error)
    }

    private suspend fun readDataLocked(context: Context, glanceId: GlanceId): ArrivalWidgetData =
        retryOnConcurrentStateAccess {
            val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
            ArrivalWidgetData.decode(prefs[ArrivalWidgetData.PREF_KEY])
        }

    private suspend fun writeDataLocked(context: Context, glanceId: GlanceId, data: ArrivalWidgetData) {
        retryOnConcurrentStateAccess {
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[ArrivalWidgetData.PREF_KEY] = data.encode()
            }
        }
        ArrivalAppWidget().update(context, glanceId)
        scheduleNextRerender(context, listOf(glanceId))
    }

    /**
     * Glance가 위젯을 그리는 동안에도 같은 상태 파일을 열기 때문에, 우리 쪽 접근과 겹치면
     * DataStore가 "multiple DataStores active" 예외를 던진다. 짧게 기다렸다 다시 시도한다.
     */
    private suspend fun <T> retryOnConcurrentStateAccess(block: suspend () -> T): T {
        var lastError: IllegalStateException? = null
        repeat(STATE_ACCESS_ATTEMPTS) { attempt ->
            try {
                return block()
            } catch (e: IllegalStateException) {
                if (e.message?.contains(MULTIPLE_DATASTORE_MESSAGE) != true) throw e
                lastError = e
                delay(STATE_ACCESS_RETRY_DELAY_MILLIS * (attempt + 1))
            }
        }
        throw lastError ?: IllegalStateException("위젯 상태 접근 실패")
    }

    private const val TAG = "ArrivalWidget"
    private const val SLOW_NETWORK_MESSAGE = "절전모드라 느려요. 다시 눌러주세요"
    private const val FAILED_MESSAGE = "새로고침에 실패했어요. 다시 눌러주세요"
    private const val FETCH_ATTEMPTS = 2
    private const val RETRY_DELAY_MILLIS = 300L
    private const val RERENDER_REQUEST_CODE = 1001
    private const val STOP_GRACE_MILLIS = 1_000L
    private const val STATE_ACCESS_ATTEMPTS = 5
    private const val STATE_ACCESS_RETRY_DELAY_MILLIS = 100L
    private const val MULTIPLE_DATASTORE_MESSAGE = "multiple DataStores active"
}
