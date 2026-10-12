package com.seungsu.ohmysubway.home.settings

import com.seungsu.ohmysubway.domain.usecase.UpdateCheck
import com.suseung.core.mvi.ViewEffect
import com.suseung.core.mvi.ViewIntent
import com.suseung.core.mvi.ViewState

sealed interface SettingsIntent : ViewIntent {
    data object Refresh : SettingsIntent
}

data class SettingsState(
    val installedVersionName: String = "",
    /** 서버 확인 결과. 아직 묻는 중이면 null. 새 빌드가 있을 때만 N 뱃지를 띄운다. */
    val updateCheck: UpdateCheck? = null,
) : ViewState

sealed interface SettingsEffect : ViewEffect
