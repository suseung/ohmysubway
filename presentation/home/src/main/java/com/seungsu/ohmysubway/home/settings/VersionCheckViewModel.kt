package com.seungsu.ohmysubway.home.settings

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.seungsu.ohmysubway.core.navigation.Screen
import com.seungsu.ohmysubway.domain.model.AppVersion
import com.seungsu.ohmysubway.domain.model.LatestBuild
import com.seungsu.ohmysubway.domain.usecase.CheckAppUpdateUseCase
import com.seungsu.ohmysubway.domain.usecase.GetInstalledVersionUseCase
import com.seungsu.ohmysubway.domain.usecase.UpdateCheck
import com.suseung.core.mvi.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VersionCheckViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val checkAppUpdate: CheckAppUpdateUseCase,
    getInstalledVersion: GetInstalledVersionUseCase,
) : MVIViewModel<VersionCheckIntent, VersionCheckState, VersionCheckEffect>() {

    private val installedName = getInstalledVersion().name

    override fun createInitialState() = VersionCheckState()

    // 설정 화면이 이미 서버에 물어본 결과를 인자로 받아 그대로 보여준다. 이 화면이 서버를 또 부르지 않는다.
    // (createInitialState 는 생성자 인자를 읽을 수 없는 시점이라 init 에서 상태를 채운다.)
    init {
        val args = savedStateHandle.toRoute<Screen.VersionCheck>()
        val status = when (args.result) {
            "UP_TO_DATE" -> VersionStatus.UpToDate
            "AVAILABLE" -> VersionStatus.Available(
                LatestBuild(AppVersion(0, args.latestName), args.note, args.pageUrl)
            )
            "UNAVAILABLE" -> VersionStatus.Unavailable
            else -> null
        }
        if (status != null) {
            setState { copy(installedName = args.installedName, status = status) }
        } else {
            // 설정 화면의 확인이 아직 안 끝난 채 들어온 드문 경우에만 직접 묻는다.
            dispatch(VersionCheckIntent.Check)
        }
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
