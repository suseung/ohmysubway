package com.seungsu.ohmysubway.widget

import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** 위젯 하나(appWidgetId 단위)의 설정 + 마지막 조회 결과 */
@Serializable
data class ArrivalWidgetData(
    val startStation: String = "",
    val destinationStation: String = "",
    val updatedAtMillis: Long = 0L,
    val loading: Boolean = false,
    val loadingStartedAtMillis: Long = 0L,
    val errorMessage: String? = null,
    val arrivals: List<WidgetArrivalItem> = emptyList(),
) {
    val configured: Boolean
        get() = startStation.isNotBlank() && destinationStation.isNotBlank()

    /**
     * 조회가 진행 중인지. 조회 도중 프로세스가 죽으면 loading이 계속 남아 위젯이 영구히
     * 새로고침되지 않으므로, 오래된 loading은 진행 중이 아닌 것으로 본다.
     */
    fun isRefreshing(nowMillis: Long): Boolean =
        loading && nowMillis - loadingStartedAtMillis < LOADING_TIMEOUT_MILLIS

    fun encode(): String = json.encodeToString(serializer(), this)

    companion object {
        const val LOADING_TIMEOUT_MILLIS = 20_000L

        val PREF_KEY = stringPreferencesKey("arrival_widget_data")

        private val json = Json { ignoreUnknownKeys = true }

        fun decode(raw: String?): ArrivalWidgetData =
            raw?.let { runCatching { json.decodeFromString(serializer(), it) }.getOrNull() }
                ?: ArrivalWidgetData()
    }
}

@Serializable
data class WidgetArrivalItem(
    val lineName: String,
    val message: String,
    val terminalStation: String,
)
