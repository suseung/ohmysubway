package com.seungsu.ohmysubway.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.seungsu.ohmysubway.domain.model.DirectedArrivals
import com.seungsu.ohmysubway.domain.usecase.GetDirectedArrivalsUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    /** 저장된 설정으로 도착정보를 다시 조회해 위젯을 갱신한다. */
    suspend fun refresh(context: Context, glanceId: GlanceId) {
        val current = stateMutex.withLock {
            val stored = readDataLocked(context, glanceId)
            val now = System.currentTimeMillis()
            if (!stored.configured) {
                // 설정이 없는(앱 재설치 등으로 상태를 잃은) 위젯도 안내 문구는 그려준다
                ArrivalAppWidget().update(context, glanceId)
                return
            }
            if (stored.isRefreshing(now)) return
            if (stored.isFresh(now)) {
                // 30초 안에 다시 눌렀으면 같은 데이터라 호출을 생략하고 화면만 다시 그린다
                ArrivalAppWidget().update(context, glanceId)
                return
            }

            stored.also {
                writeDataLocked(context, glanceId, it.copy(loading = true, loadingStartedAtMillis = now))
            }
        }

        val useCase = EntryPointAccessors
            .fromApplication(context, WidgetEntryPoint::class.java)
            .getDirectedArrivalsUseCase()

        val refreshed = runCatching {
            useCase(
                GetDirectedArrivalsUseCase.Params(
                    startStation = current.startStation,
                    destinationStation = current.destinationStation,
                ),
            )
        }.fold(
            onSuccess = { result ->
                when (result) {
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
                            arrivals = result.arrivals.take(MAX_WIDGET_ARRIVALS).map { directed ->
                                // 데이터 지연을 보정한 남은 시간으로 도착 예정 시각을 만든다
                                val remaining = directed.arrival.remainingSeconds(fetchedAt)
                                WidgetArrivalItem(
                                    lineName = directed.lineName,
                                    message = directed.arrival.arrivalMessage,
                                    terminalStation = directed.arrival.terminalStation,
                                    arrivalAtMillis = remaining?.let { fetchedAt + it * 1000L },
                                )
                            },
                        )
                    }
                }
            },
            onFailure = {
                current.copy(loading = false, errorMessage = "새로고침에 실패했어요. 다시 눌러주세요")
            },
        )

        stateMutex.withLock { writeDataLocked(context, glanceId, refreshed) }
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

    private const val STATE_ACCESS_ATTEMPTS = 5
    private const val STATE_ACCESS_RETRY_DELAY_MILLIS = 100L
    private const val MULTIPLE_DATASTORE_MESSAGE = "multiple DataStores active"
}
