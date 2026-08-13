package com.seungsu.ohmysubway.guide

/**
 * 노선별 실시간 API 응답 특성. 2026-08-12 18~19시에 역 8곳에서 98편을 표본으로 측정한 값이다.
 * 실측 기록이라 시간대·운행 상황에 따라 달라질 수 있어 화면에도 측정 조건을 함께 표기한다.
 */
data class LineDelayInfo(
    val lineName: String,
    val medianDelayText: String,
    val rangeText: String,
    /** barvlDt(남은 초)를 실제로 주는 비율 */
    val secondsProvidedPercent: Int,
    val note: String? = null,
)

/** 초 단위 남은 시간을 주는 노선 — 지연 보정이 적용된다. */
val LINES_WITH_SECONDS = listOf(
    LineDelayInfo("2·3·5호선", "40초", "35~46초", 100),
    LineDelayInfo("4호선", "115초", "39초~2분 45초", 43),
    LineDelayInfo("1호선", "85초", "35초~7분 40초", 19),
)

/** 초 단위를 주지 않는 노선 — "N번째 전역" 문구만 표시된다. */
val LINES_WITHOUT_SECONDS = listOf(
    LineDelayInfo("8호선", "112초", "85~140초", 0),
    LineDelayInfo("공항철도", "129초", "111초~10분 52초", 0),
    LineDelayInfo("경의중앙선", "161초", "60초~5분 54초", 0),
    LineDelayInfo("수인분당선", "253초", "100초~7분 47초", 0),
    LineDelayInfo(
        lineName = "신분당선",
        medianDelayText = "-93초",
        rangeText = "-128~-59초",
        secondsProvidedPercent = 0,
        note = "운영사 서버 시계가 우리보다 앞서 있어, 보정하면 오히려 시간이 늘어납니다. 그래서 이 노선은 보정하지 않습니다.",
    ),
)
