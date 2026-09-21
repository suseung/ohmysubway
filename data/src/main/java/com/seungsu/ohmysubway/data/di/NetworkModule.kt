package com.seungsu.ohmysubway.data.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.seungsu.ohmysubway.data.BuildConfig
import com.seungsu.ohmysubway.data.service.SubwayApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides @Singleton
    fun provideChuckerInterceptor(
        @ApplicationContext context: Context
    ): ChuckerInterceptor? {
        return if (BuildConfig.DEBUG) {
            ChuckerInterceptor.Builder(context)
                .collector(ChuckerCollector(context))
                .maxContentLength(250_000L)
                .alwaysReadResponseBody(true)
                .build()
        } else {
            null
        }
    }

    @Provides @Singleton
    fun provideOkHttpClient(
        chuckerInterceptor: ChuckerInterceptor?
    ): OkHttpClient = OkHttpClient.Builder()
        // 위젯 탭은 브로드캐스트로 처리돼 시간이 얼마 없다. 30초를 기다리면
        // 절전모드에서 응답 전에 프로세스가 잘려 "불러오는 중"만 남는다.
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .callTimeout(7, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .apply {
            if (BuildConfig.DEBUG && chuckerInterceptor != null) {
                addInterceptor(chuckerInterceptor)
            }
        }
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl("http://swopenapi.seoul.go.kr/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
        .build()

    @Provides @Singleton
    fun provideSubwayApiService(retrofit: Retrofit): SubwayApiService =
        retrofit.create(SubwayApiService::class.java)
}
