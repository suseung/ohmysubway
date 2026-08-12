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

/** 위젯 상태 저장/새로고침 담당. Glance 콜백에서는 Hilt 주입이 안 되므로 EntryPoint로 접근한다. */
object ArrivalWidgetUpdater {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun getDirectedArrivalsUseCase(): GetDirectedArrivalsUseCase
    }

    private const val MAX_WIDGET_ARRIVALS = 4

    /** 위젯 설정을 저장하고 첫 조회까지 수행한다. */
    suspend fun configure(context: Context, glanceId: GlanceId, startStation: String, destinationStation: String) {
        writeData(
            context, glanceId,
            ArrivalWidgetData(startStation = startStation, destinationStation = destinationStation),
        )
        refresh(context, glanceId)
    }

    /** 저장된 설정으로 도착정보를 다시 조회해 위젯을 갱신한다. */
    suspend fun refresh(context: Context, glanceId: GlanceId) {
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
        val current = ArrivalWidgetData.decode(prefs[ArrivalWidgetData.PREF_KEY])
        val now = System.currentTimeMillis()
        if (!current.configured || current.isRefreshing(now)) return

        writeData(context, glanceId, current.copy(loading = true, loadingStartedAtMillis = now))

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

                    is DirectedArrivals.Success -> current.copy(
                        loading = false,
                        errorMessage = null,
                        updatedAtMillis = System.currentTimeMillis(),
                        arrivals = result.arrivals.take(MAX_WIDGET_ARRIVALS).map {
                            WidgetArrivalItem(
                                lineName = it.lineName,
                                message = it.arrival.arrivalMessage,
                                terminalStation = it.arrival.terminalStation,
                            )
                        },
                    )
                }
            },
            onFailure = {
                current.copy(loading = false, errorMessage = "새로고침에 실패했어요. 다시 눌러주세요")
            },
        )

        writeData(context, glanceId, refreshed)
    }

    private suspend fun writeData(context: Context, glanceId: GlanceId, data: ArrivalWidgetData) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[ArrivalWidgetData.PREF_KEY] = data.encode()
        }
        ArrivalAppWidget().update(context, glanceId)
    }
}
