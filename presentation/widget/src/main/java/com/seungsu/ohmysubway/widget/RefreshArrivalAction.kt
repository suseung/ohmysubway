package com.seungsu.ohmysubway.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import kotlinx.coroutines.CancellationException

class RefreshArrivalAction : ActionCallback {

    /**
     * 여기서 예외가 밖으로 나가면 런처가 위젯을 못 그려 "위젯을 로드할 수 없음"이 뜬다.
     * 실패는 위젯 안에 문구로 남기고, 예외 자체는 삼킨다. (취소는 그대로 전달)
     */
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        try {
            ArrivalWidgetUpdater.refresh(context, glanceId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 위젯 상태에 이미 실패가 기록되어 있다
        }
    }
}
