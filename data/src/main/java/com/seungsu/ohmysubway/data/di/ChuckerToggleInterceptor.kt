package com.seungsu.ohmysubway.data.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.suseung.core.debug.DebugPreferences
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Chucker 를 요청마다 켜고 끈다.
 *
 * OkHttpClient 는 Singleton 으로 한 번 만들어지고 인터셉터 목록은 그 뒤로 못 바꾼다.
 * 그래서 스위치를 [ChuckerInterceptor] 자체를 넣었다 뺐다 하는 식으로 만들면 앱을
 * 다시 띄워야 반영된다. 대신 항상 이 래퍼를 끼워두고 안에서 매 요청 깃발을 보면,
 * 드로어에서 끄는 즉시 다음 요청부터 기록이 멈춘다.
 *
 * 꺼져 있을 때는 그냥 통과시키므로 오버헤드는 불리언 읽기 하나다.
 */
class ChuckerToggleInterceptor(
    private val context: Context,
    private val delegate: ChuckerInterceptor,
    /**
     * 어느 스위치를 볼지. 기본은 공용 스위치다. 따로 켜고 꺼야 하는 경로
     * (예: 토큰과 개인 기록이 오가는 백업)는 자기 깃발을 넘긴다.
     */
    private val isEnabled: (Context) -> Boolean = DebugPreferences::isChuckerEnabled,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response =
        if (isEnabled(context)) {
            delegate.intercept(chain)
        } else {
            chain.proceed(chain.request())
        }
}
