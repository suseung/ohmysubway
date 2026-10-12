package com.seungsu.ohmysubway.core.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {

    @Serializable
    data object Home : Screen

    @Serializable
    data object Guide : Screen

    /** 버전확인만 들어 있는 설정 화면. */
    @Serializable
    data object Settings : Screen

    /**
     * 설치된 버전이 최신인지 보여주고, 새 버전이 있으면 받으러 간다.
     * 서버 확인은 설정 화면이 한 번 하고, 그 결과를 이 인자로 넘겨받는다 — 이 화면이 서버를 또 부르지 않는다.
     * [result] 는 설정 화면의 확인이 아직 안 끝났을 때만 null 이고, 그때만 이 화면이 직접 묻는다.
     */
    @Serializable
    data class VersionCheck(
        val installedName: String,
        /** "UP_TO_DATE" · "AVAILABLE" · "UNAVAILABLE", 확인 중이면 null. */
        val result: String? = null,
        val latestName: String = "",
        val note: String = "",
        /** 서버의 앱 페이지 주소. */
        val pageUrl: String = "",
    ) : Screen
}
