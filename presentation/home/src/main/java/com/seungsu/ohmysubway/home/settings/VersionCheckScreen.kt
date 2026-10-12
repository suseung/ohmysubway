package com.seungsu.ohmysubway.home.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suseung.designsystem.theme.SsTheme

@Composable
fun VersionCheckScreen(
    onBack: () -> Unit,
    viewModel: VersionCheckViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiAction = remember { viewModel::dispatch }
    val context = LocalContext.current
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
                text = "버전확인",
                style = SsTheme.typos.bold.font18,
                color = colors.label.onBgPrimary,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val status = state.status
            AppIcon(modifier = Modifier.padding(bottom = 32.dp))
            Text(
                text = when (status) {
                    VersionStatus.Checking -> "확인하는 중이에요…"
                    VersionStatus.UpToDate -> "현재 최신 버전을 이용 중입니다."
                    is VersionStatus.Available -> "새 버전이 있어요."
                    VersionStatus.Unavailable -> "확인할 수 없어요."
                },
                style = SsTheme.typos.bold.font18,
                color = colors.label.onBgPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "현재 버전 ${state.installedName}",
                style = SsTheme.typos.regular.font14,
                color = colors.label.onBgSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (status is VersionStatus.Available) {
                Text(
                    text = "새 버전 ${status.latest.version.name}",
                    style = SsTheme.typos.regular.font14,
                    color = colors.label.onBgSecondary,
                    modifier = Modifier.padding(top = 8.dp),
                )
                if (status.latest.note.isNotBlank()) {
                    Text(
                        text = status.latest.note,
                        style = SsTheme.typos.regular.font14,
                        color = colors.label.onBgTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            if (status == VersionStatus.Unavailable) {
                Text(
                    text = "배포 서버에 닿지 않았어요.",
                    style = SsTheme.typos.regular.font12,
                    color = colors.label.onBgTertiary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            // 최신이면 흐리게 막고, 새 버전이 있을 때만 받으러 간다. 못 확인했을 땐 다시 확인.
            // 채운 버튼은 accent 가 아니라 테마 토큰으로 칠한다(accent 가 회색이라 다크에서 묻힌다).
            Button(
                onClick = {
                    when (status) {
                        is VersionStatus.Available -> runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(status.latest.pageUrl))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        }
                        VersionStatus.Unavailable -> uiAction(VersionCheckIntent.Check)
                        else -> Unit
                    }
                },
                enabled = status is VersionStatus.Available || status == VersionStatus.Unavailable,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.label.onBgPrimary,
                    contentColor = colors.background.defaultBase,
                ),
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth(),
            ) {
                Text(if (status == VersionStatus.Unavailable) "다시 확인" else "최신 버전 업데이트")
            }
        }
    }
}

/**
 * 설치된 앱의 런처 아이콘. AdaptiveIconDrawable 은 그릴 때 가장자리 여백을 알아서 잘라내므로
 * 따로 확대하지 않고 둥근 사각형으로만 자른다.
 */
@Composable
private fun AppIcon(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember {
        runCatching {
            context.packageManager.getApplicationIcon(context.packageName).toBitmap(324, 324)
        }.getOrNull()
    } ?: return
    Box(modifier = modifier.size(96.dp).clip(RoundedCornerShape(22.dp))) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
