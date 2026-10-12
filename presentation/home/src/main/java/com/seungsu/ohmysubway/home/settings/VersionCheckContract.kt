package com.seungsu.ohmysubway.home.settings

import com.seungsu.ohmysubway.domain.model.LatestBuild
import com.suseung.core.mvi.ViewEffect
import com.suseung.core.mvi.ViewIntent
import com.suseung.core.mvi.ViewState

sealed interface VersionCheckIntent : ViewIntent {
    data object Check : VersionCheckIntent
}

sealed interface VersionStatus {
    data object Checking : VersionStatus
    data object UpToDate : VersionStatus
    data class Available(val latest: LatestBuild) : VersionStatus
    data object Unavailable : VersionStatus
}

data class VersionCheckState(
    val installedName: String = "",
    val status: VersionStatus = VersionStatus.Checking,
) : ViewState

sealed interface VersionCheckEffect : ViewEffect
