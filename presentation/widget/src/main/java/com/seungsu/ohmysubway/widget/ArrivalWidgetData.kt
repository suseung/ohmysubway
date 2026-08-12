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
    val appearance: WidgetAppearance = WidgetAppearance(),
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

/**
 * 위젯 외형. 배경색과 투명도만 사용자가 고른다.
 * 글자색은 배경 밝기에 따라 자동으로 잘 보이는 쪽(검정/흰색)으로 정해지므로 별도 설정이 없다.
 */
@Serializable
data class WidgetAppearance(
    val backgroundArgb: Int = DEFAULT_BACKGROUND_ARGB,
    val backgroundAlpha: Float = DEFAULT_BACKGROUND_ALPHA,
) {
    companion object {
        const val DEFAULT_BACKGROUND_ARGB = 0xFF1A1A1A.toInt()
        const val DEFAULT_BACKGROUND_ALPHA = 0.85f
    }
}
