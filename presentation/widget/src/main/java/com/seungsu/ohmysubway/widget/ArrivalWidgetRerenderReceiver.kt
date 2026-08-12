package com.seungsu.ohmysubway.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 카운트다운이 0을 지나면 음수로 표시되므로, 열차가 도착할 무렵 위젯을 한 번 다시 그린다.
 * 네트워크 호출은 하지 않고 화면만 갱신한다 — 새 데이터 조회는 사용자가 탭할 때만.
 */
class ArrivalWidgetRerenderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_RERENDER) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            try {
                ArrivalWidgetUpdater.rerenderAll(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_RERENDER = "com.seungsu.ohmysubway.widget.action.RERENDER"
    }
}
