package com.seungsu.ohmysubway.data.usage

import android.content.Context
import android.content.SharedPreferences
import com.seungsu.ohmysubway.data.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 외부 API 호출 횟수를 모아 배포 서버의 POST /api/usage 로 보고한다.
 *
 * 위젯은 응답을 받고 나면 프로세스가 금방 잘릴 수 있어서, 센 값은 먼저 SharedPreferences 에
 * 적어두고 나중에 보낸다. 못 보내면(서버가 꺼져 있거나 오프라인) 그대로 남겨뒀다가 다음 기회에 보낸다.
 * 호출을 막지 않도록 보고는 전부 백그라운드에서, 앱이 쓰는 OkHttpClient 와 별개 클라이언트로 한다
 * (같은 클라이언트면 보고 요청이 인터셉터를 다시 지나고 타임아웃도 뒤섞인다).
 */
@Singleton
class UsageReporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("api_usage_pending", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
    private val executor = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "usage-reporter").apply { isDaemon = true }
    }
    private val enabled = BuildConfig.APK_DIST_URL.isNotBlank() && BuildConfig.APK_DIST_READ_TOKEN.isNotBlank()
    private var flushScheduled = false
    private var sending = false

    fun record(api: String, success: Boolean) {
        if (!enabled) return
        synchronized(this) {
            prefs.edit()
                .putInt("$api.n", prefs.getInt("$api.n", 0) + 1)
                .putInt("$api.e", prefs.getInt("$api.e", 0) + if (success) 0 else 1)
                .apply()
            if (!flushScheduled) {
                flushScheduled = true
                executor.schedule({ flush() }, FLUSH_DELAY_SEC, TimeUnit.SECONDS)
            }
        }
    }

    private fun flush() {
        val snapshot = synchronized(this) {
            flushScheduled = false
            if (sending) return
            val counts = LinkedHashMap<String, Pair<Int, Int>>()
            for (key in prefs.all.keys) {
                if (!key.endsWith(".n")) continue
                val api = key.removeSuffix(".n")
                val n = prefs.getInt(key, 0)
                if (n > 0) counts[api] = n to prefs.getInt("$api.e", 0)
            }
            if (counts.isEmpty()) return
            // 보내는 동안 새로 쌓이는 값과 섞이지 않게, 보낼 만큼을 미리 덜어낸다.
            prefs.edit().apply { counts.keys.forEach { remove("$it.n"); remove("$it.e") } }.apply()
            sending = true
            counts
        }

        val events = JSONArray().apply {
            snapshot.forEach { (api, c) ->
                put(JSONObject().put("api", api).put("n", c.first).put("e", minOf(c.second, c.first)))
            }
        }
        val body = JSONObject().put("appId", context.packageName).put("events", events).toString()
        val request = Request.Builder()
            .url("${BuildConfig.APK_DIST_URL}/api/usage")
            .header("Authorization", "Bearer ${BuildConfig.APK_DIST_READ_TOKEN}")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = giveBack(snapshot)
            override fun onResponse(call: Call, response: Response) {
                response.use { if (!it.isSuccessful) giveBack(snapshot) else synchronized(this@UsageReporter) { sending = false } }
            }
        })
    }

    /** 못 보낸 횟수를 되돌려 놓는다. 서버가 꺼져 있는 동안 무한정 쌓이지 않게 상한을 둔다. */
    private fun giveBack(snapshot: Map<String, Pair<Int, Int>>) {
        synchronized(this) {
            prefs.edit().apply {
                snapshot.forEach { (api, c) ->
                    putInt("$api.n", minOf(prefs.getInt("$api.n", 0) + c.first, MAX_PENDING))
                    putInt("$api.e", minOf(prefs.getInt("$api.e", 0) + c.second, MAX_PENDING))
                }
            }.apply()
            sending = false
        }
    }

    private companion object {
        const val FLUSH_DELAY_SEC = 10L
        const val MAX_PENDING = 10_000
    }
}
