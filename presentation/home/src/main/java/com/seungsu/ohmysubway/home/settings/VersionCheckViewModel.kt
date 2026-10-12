package com.seungsu.ohmysubway.home.settings

import com.seungsu.ohmysubway.domain.usecase.CheckAppUpdateUseCase
import com.seungsu.ohmysubway.domain.usecase.GetInstalledVersionUseCase
import com.seungsu.ohmysubway.domain.usecase.UpdateCheck
import com.suseung.core.mvi.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VersionCheckViewModel @Inject constructor(
    private val checkAppUpdate: CheckAppUpdateUseCase,
    getInstalledVersion: GetInstalledVersionUseCase,
) : MVIViewModel<VersionCheckIntent, VersionCheckState, VersionCheckEffect>() {

    private val installedName = getInstalledVersion().name

    override fun createInitialState() = VersionCheckState()

    init {
        dispatch(VersionCheckIntent.Check)
    }

    override suspend fun processIntent(intent: VersionCheckIntent) {
        when (intent) {
            VersionCheckIntent.Check -> {
                setState { copy(installedName = this@VersionCheckViewModel.installedName, status = VersionStatus.Checking) }
                val status = when (val result = checkAppUpdate(Unit)) {
                    UpdateCheck.UpToDate -> VersionStatus.UpToDate
                    is UpdateCheck.Available -> VersionStatus.Available(result.latest)
                    UpdateCheck.Unavailable -> VersionStatus.Unavailable
                }
                setState { copy(status = status) }
            }
        }
    }
}
