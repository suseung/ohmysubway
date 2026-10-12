package com.seungsu.ohmysubway.home.settings

import com.suseung.core.mvi.ViewEffect
import com.suseung.core.mvi.ViewIntent
import com.suseung.core.mvi.ViewState

sealed interface SettingsIntent : ViewIntent {
    data object Refresh : SettingsIntent
}

data class SettingsState(
    val installedVersionName: String = "",
    /** 서버에 새 빌드가 있을 때만 true. 못 확인했으면 false 라 뱃지가 뜨지 않는다. */
    val updateAvailable: Boolean = false,
) : ViewState

sealed interface SettingsEffect : ViewEffect
