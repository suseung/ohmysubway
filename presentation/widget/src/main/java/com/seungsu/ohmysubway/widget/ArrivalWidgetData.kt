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
     * 조회가 진행 중인지. 절전모드에서는 조회 도중 프로세스가 얼어붙거나 죽어서 loading이
     * 그대로 남는 일이 잦다. 그러면 위젯이 영원히 "불러오는 중"으로 보이므로,
     * 조회에 허용한 시간(REFRESH_BUDGET)을 넘긴 loading은 진행 중이 아닌 것으로 본다.
     * 표시할 때도 이 값을 쓰기 때문에 남은 loading 표시가 저절로 풀린다.
     */
    fun isRefreshing(nowMillis: Long): Boolean =
        loading && nowMillis - loadingStartedAtMillis < LOADING_TIMEOUT_MILLIS

    /**
     * 조회에 실패했을 때 직전 결과를 그대로 보여줄 만한지.
     * 실패할 때마다 화면을 비우면 절전모드에서 위젯이 계속 빈 채로 남는다.
     */
    fun hasUsableArrivals(nowMillis: Long): Boolean =
        arrivals.isNotEmpty() && updatedAtMillis > 0 &&
            nowMillis - updatedAtMillis < USABLE_ARRIVALS_MILLIS

    /**
     * API는 30초 주기로만 갱신되므로 그 안에 다시 조회해도 같은 데이터가 온다.
     * 불필요한 호출(일 1,000회 제한)을 아끼기 위해 최근 데이터는 그대로 쓴다.
     */
    fun isFresh(nowMillis: Long): Boolean =
        updatedAtMillis > 0 && nowMillis - updatedAtMillis < DATA_REFRESH_INTERVAL_MILLIS

    fun encode(): String = json.encodeToString(serializer(), this)

    companion object {
        /**
         * 한 번의 새로고침에 허용하는 시간. 위젯 탭은 브로드캐스트로 처리되는데,
         * 절전모드에서는 이 짧은 처리 시간이 지나면 프로세스가 곧바로 얼어붙는다.
         * 그 전에 반드시 결과(성공이든 실패든)를 써야 해서 넉넉하지 않게 잡는다.
         */
        const val REFRESH_BUDGET_MILLIS = 8_000L

        /** 예산보다 조금 길게 — 결과를 쓰는 시간까지 감안한 loading 유효기간 */
        const val LOADING_TIMEOUT_MILLIS = 10_000L
        const val DATA_REFRESH_INTERVAL_MILLIS = 30_000L

        /** 이 시간 안에 받아둔 데이터면 새로고침이 실패해도 계속 보여준다 */
        const val USABLE_ARRIVALS_MILLIS = 3 * 60_000L

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
    /** 초 단위 정보가 없는 노선에서 쓰는 서버 문구 (예: "[3]번째 전역") */
    val message: String,
    val terminalStation: String,
    /** 초 단위 정보가 있으면 도착 예정 시각. 위젯이 여기서부터 카운트다운한다. */
    val arrivalAtMillis: Long? = null,
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
        /** 프리셋 첫 번째(1호선)와 같게 둔다 — 기본값이 목록에 없으면 선택 표시가 안 된다. */
        const val DEFAULT_BACKGROUND_ARGB = 0xFF0052A4.toInt()
        const val DEFAULT_BACKGROUND_ALPHA = 0.85f
    }
}
