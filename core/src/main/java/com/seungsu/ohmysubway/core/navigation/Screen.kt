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

    /** 설치된 버전이 최신인지 확인하고, 새 버전이 있으면 받으러 간다. */
    @Serializable
    data object VersionCheck : Screen
}
