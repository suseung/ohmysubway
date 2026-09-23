package com.seungsu.ohmysubway.core.debug

import android.content.Context

/**
 * 디버그 드로어가 바꾸는 값들을 담는다.
 *
 * DataStore 가 아니라 SharedPreferences 를 쓴다. 읽는 쪽이 OkHttp 인터셉터인데,
 * 인터셉터는 정지 함수가 아니라 값을 그 자리에서 동기로 꺼내야 한다. DataStore 로
 * 하려면 runBlocking 을 끼워야 하고, 그러면 네트워크 스레드를 잠그는 지점이 하나
 * 생긴다. 디버그 스위치 하나 때문에 그럴 이유가 없다.
 *
 * 값은 [cached] 에 들고 있는다. 인터셉터는 요청마다 도는 자리라 매번 디스크를
 * 치면 안 된다.
 */
object DebugPreferences {

    private const val FILE_NAME = "ohmy_debug_prefs"
    private const val KEY_CHUCKER_ENABLED = "chucker_enabled"

    /**
     * **기본은 꺼짐이다.**
     *
     * 켜 두면 디버그로 앱을 쓰는 동안 모든 요청·응답이 기기 안 DB 로 계속 쌓인다.
     * 로그인 토큰이나 사용자가 쓴 글이 오가는 앱이면 그게 그대로 남는다. 볼 이유가
     * 있을 때 사람이 직접 켜는 게 맞다.
     */
    private const val DEFAULT_CHUCKER_ENABLED = false

    @Volatile
    private var cached: Boolean? = null

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun isChuckerEnabled(context: Context): Boolean =
        cached ?: prefs(context)
            .getBoolean(KEY_CHUCKER_ENABLED, DEFAULT_CHUCKER_ENABLED)
            .also { cached = it }

    fun setChuckerEnabled(context: Context, enabled: Boolean) {
        cached = enabled
        prefs(context).edit().putBoolean(KEY_CHUCKER_ENABLED, enabled).apply()
    }
}
