package com.seungsu.ohmysubway.home.settings

import com.seungsu.ohmysubway.domain.usecase.CheckAppUpdateUseCase
import com.seungsu.ohmysubway.domain.usecase.GetInstalledVersionUseCase
import com.suseung.core.mvi.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val checkAppUpdate: CheckAppUpdateUseCase,
    private val getInstalledVersion: GetInstalledVersionUseCase,
) : MVIViewModel<SettingsIntent, SettingsState, SettingsEffect>() {

    override fun createInitialState() = SettingsState()

    // 화면이 열릴 때마다 한 번 묻는다.
    init {
        dispatch(SettingsIntent.Refresh)
    }

    override suspend fun processIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.Refresh -> {
                setState { copy(installedVersionName = getInstalledVersion().name) }
                // 설정 화면이 열릴 때 init 에서 한 번만 묻는다. 결과는 N 뱃지에 쓰고,
                // 버전확인 화면에도 이동 인자로 그대로 넘긴다. 서버에 닿지 않으면 뱃지를 띄우지 않는다.
                val result = checkAppUpdate(Unit)
                setState { copy(updateCheck = result) }
            }
        }
    }
}
