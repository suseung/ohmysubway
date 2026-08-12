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
    val errorMessage: String? = null,
    val arrivals: List<WidgetArrivalItem> = emptyList(),
) {
    val configured: Boolean
        get() = startStation.isNotBlank() && destinationStation.isNotBlank()

    fun encode(): String = json.encodeToString(serializer(), this)

    companion object {
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
