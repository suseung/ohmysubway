package com.seungsu.ohmysubway.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seungsu.ohmysubway.core.navigation.Screen
import com.seungsu.ohmysubway.domain.usecase.UpdateCheck
import com.suseung.designsystem.theme.SsTheme

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToVersionCheck: (Screen.VersionCheck) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SsTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.defaultBase)
            .systemBarsPadding(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로",
                    tint = colors.label.onBgPrimary,
                )
            }
            Text(
                text = "설정",
                style = SsTheme.typos.bold.font18,
                color = colors.label.onBgPrimary,
            )
        }
        VersionRow(
            versionName = state.installedVersionName,
            updateAvailable = state.updateCheck is UpdateCheck.Available,
            // 설정 화면이 받은 확인 결과를 그대로 넘긴다 — 버전확인 화면이 서버를 또 부르지 않는다.
            onClick = { onNavigateToVersionCheck(state.toVersionCheckRoute()) },
        )
    }
}

@Composable
private fun VersionRow(versionName: String, updateAvailable: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "버전확인",
            style = SsTheme.typos.regular.font16,
            color = SsTheme.colors.label.onBgPrimary,
            modifier = Modifier.weight(1f),
        )
        if (updateAvailable) {
            // 새 버전이 있다는 표시. 회색 accent 가 아니라 고정 빨강이라 다크에서도 보인다.
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(20.dp)
                    .background(SsTheme.colors.system.red, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("N", style = SsTheme.typos.bold.font12, color = Color.White)
            }
        }
        Text(
            text = versionName,
            style = SsTheme.typos.regular.font14,
            color = SsTheme.colors.label.onBgSecondary,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = SsTheme.colors.label.onBgTertiary,
        )
    }
}

/** 설정 화면의 확인 결과를 버전확인 화면의 이동 인자로 바꾼다. */
private fun SettingsState.toVersionCheckRoute(): Screen.VersionCheck = when (val check = updateCheck) {
    UpdateCheck.UpToDate -> Screen.VersionCheck(installedName = installedVersionName, result = "UP_TO_DATE")
    is UpdateCheck.Available -> Screen.VersionCheck(
        installedName = installedVersionName,
        result = "AVAILABLE",
        latestName = check.latest.version.name,
        note = check.latest.note,
        pageUrl = check.latest.pageUrl,
    )
    UpdateCheck.Unavailable -> Screen.VersionCheck(installedName = installedVersionName, result = "UNAVAILABLE")
    null -> Screen.VersionCheck(installedName = installedVersionName)   // 아직 묻는 중 — 그 화면이 직접 묻는다
}
