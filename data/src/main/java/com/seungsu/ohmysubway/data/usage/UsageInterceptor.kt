package com.seungsu.ohmysubway.data.usage

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 외부 API 호출을 센다. 요청 내용은 보지 않고 호스트와 성공 여부만 본다.
 * 호출 자체에는 손대지 않는다 — 센 뒤 그대로 돌려준다.
 */
class UsageInterceptor(private val reporter: UsageReporter) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val key = UsageKeys.forHost(request.url.host) ?: return chain.proceed(request)
        return try {
            chain.proceed(request).also { reporter.record(key, it.isSuccessful) }
        } catch (e: IOException) {
            reporter.record(key, false)
            throw e
        }
    }
}
