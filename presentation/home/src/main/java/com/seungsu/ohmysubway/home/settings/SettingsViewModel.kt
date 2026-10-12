package com.seungsu.ohmysubway.home.settings

import com.seungsu.ohmysubway.domain.usecase.CheckAppUpdateUseCase
import com.seungsu.ohmysubway.domain.usecase.GetInstalledVersionUseCase
import com.seungsu.ohmysubway.domain.usecase.UpdateCheck
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
                // 서버에 닿지 않으면 뱃지를 띄우지 않는다.
                val available = checkAppUpdate(Unit) is UpdateCheck.Available
                setState { copy(updateAvailable = available) }
            }
        }
    }
}
