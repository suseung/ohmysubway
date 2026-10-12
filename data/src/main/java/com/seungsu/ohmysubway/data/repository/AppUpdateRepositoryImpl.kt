package com.seungsu.ohmysubway.data.repository

import android.content.Context
import com.seungsu.ohmysubway.data.BuildConfig
import com.seungsu.ohmysubway.domain.model.AppVersion
import com.seungsu.ohmysubway.domain.model.LatestBuild
import com.seungsu.ohmysubway.domain.repository.AppUpdateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * apk-dist 서버의 `/api/latest/{applicationId}` 로 최신 빌드를 묻는다.
 * 서버는 맥에서 돌고 꺼져 있을 수 있으므로 타임아웃을 짧게 잡는다.
 *
 * 공용 OkHttpClient(HttpLoggingInterceptor BODY + ApiUsageInterceptor)는 쓰지 않는다 —
 * 읽기 토큰이 담긴 Authorization 헤더가 logcat 에 남고, 버전확인 호출이 외부 API 호출 횟수에 섞인다.
 */
@Singleton
class AppUpdateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppUpdateRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()

    override fun installed(): AppVersion {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return AppVersion(code = info.longVersionCode, name = info.versionName.orEmpty())
    }

    override suspend fun fetchLatest(): LatestBuild {
        val request = Request.Builder()
            .url("${BuildConfig.APK_DIST_URL}/api/latest/${context.packageName}")
            .header("Authorization", "Bearer ${BuildConfig.APK_DIST_READ_TOKEN}")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            val json = JSONObject(response.body.string())
            return LatestBuild(
                version = AppVersion(
                    code = json.getLong("versionCode"),
                    name = json.optString("versionName"),
                ),
                note = json.optString("note"),
                // 응답의 downloadUrl 은 로그인이 필요한 직접 다운로드라 쓰지 않는다. 앱 페이지를 연다.
                pageUrl = "${BuildConfig.APK_DIST_URL}/a/${context.packageName}",
            )
        }
    }
}
