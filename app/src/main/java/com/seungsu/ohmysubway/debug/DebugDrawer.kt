package com.seungsu.ohmysubway.debug

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.chuckerteam.chucker.api.Chucker
import com.seungsu.ohmysubway.BuildConfig
import com.seungsu.ohmysubway.design.compose.BuildConfig as DesignSystemBuildConfig
import com.seungsu.ohmysubway.core.BuildConfig as CoreBuildConfig
import com.suseung.core.debug.DebugPreferences
import kotlinx.coroutines.launch

/**
 * 타이틀 바 왼쪽(네비게이션 아이콘)과 오른쪽(있다면 액션 아이콘) 버튼을 건드리지
 * 않도록, 그 사이 빈 공간만 손잡이로 쓴다. 양쪽 아이콘 터치 영역(48dp)보다
 * 넉넉하게 잡는다.
 */
private val TITLE_ZONE_INSET = 72.dp

/** 타이틀 바 높이보다 조금 더 넉넉하게 — 상태바 포함해서 길게 눌러도 걸리게. */
private val TITLE_ZONE_HEIGHT = 96.dp

private val DRAWER_WIDTH = 300.dp

/**
 * 타이틀을 길게 누르면 열리는 디버그 서랍.
 *
 * 릴리즈 빌드에서는 [content] 를 그대로 내보내고 아무것도 감싸지 않는다.
 *
 * **가장자리 스와이프 대신 길게 누르기를 쓰는 이유.** 왼쪽·오른쪽 가장자리는 시스템
 * 뒤로가기 제스처 구역이라 스와이프로 여는 손잡이는 계속 걸리적거렸다(2026-10-10).
 * 타이틀 바 빈 공간을 길게 누르는 건 어떤 시스템 제스처와도 안 겹치고, 짧게 누르는
 * 보통 탭과도 구분된다.
 *
 * **왜 레이아웃 방향을 뒤집나.** ModalNavigationDrawer 는 언제나 시작 모서리에서
 * 나온다. 오른쪽에서 나오게 하는 옵션이 없어서, 드로어만 RTL 로 계산시키고 그 안의
 * 실제 내용은 다시 LTR 로 되돌려 놓는다. 내용까지 RTL 로 두면 앱 화면 전체가
 * 거울처럼 뒤집힌다.
 */
@Composable
fun DebugDrawerHost(content: @Composable () -> Unit) {
    if (!BuildConfig.DEBUG) {
        content()
        return
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val setDrawerOpen: (Boolean) -> Unit = { shouldOpen ->
        scope.launch {
            if (shouldOpen) drawerState.open() else drawerState.close()
        }
    }

    BackHandler(enabled = drawerState.isOpen) { setDrawerOpen(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    DebugPanel(onClose = { setDrawerOpen(false) })
                }
            },
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = TITLE_ZONE_INSET)
                            .height(TITLE_ZONE_HEIGHT)
                            .zIndex(1f)
                            .pointerInput(Unit) {
                                detectTapGestures(onLongPress = { setDrawerOpen(true) })
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun DebugPanel(onClose: () -> Unit) {
    val context = LocalContext.current
    var chuckerEnabled by remember { mutableStateOf(DebugPreferences.isChuckerEnabled(context)) }

    ModalDrawerSheet(modifier = Modifier.width(DRAWER_WIDTH)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Debug",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        SectionLabel("Chucker")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "네트워크 기록", style = MaterialTheme.typography.bodyMedium)
                Text(
                    // 껐다 켠 게 언제부터 먹는지 헷갈리지 않게 적어둔다.
                    text = if (chuckerEnabled) "다음 요청부터 기록한다" else "기록하지 않는다",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = chuckerEnabled,
                onCheckedChange = { enabled ->
                    chuckerEnabled = enabled
                    DebugPreferences.setChuckerEnabled(context, enabled)
                    if (!enabled) Chucker.dismissNotifications(context)
                },
            )
        }
        NavigationDrawerItem(
            label = { Text("기록 열기") },
            selected = false,
            onClick = {
                openChucker(context)
                onClose()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        SectionLabel("Build")
        InfoRow("applicationId", BuildConfig.APPLICATION_ID)
        InfoRow("version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        InfoRow("designsystem", DesignSystemBuildConfig.DESIGNSYSTEM_VERSION)

        InfoRow("core", CoreBuildConfig.CORE_VERSION)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp),
        )
        Text(text = value, style = MaterialTheme.typography.labelSmall)
    }
}

/**
 * Chucker 화면을 띄운다.
 *
 * 릴리즈에서는 no-op 라이브러리가 들어가 이 인텐트가 아무 화면도 가리키지 않는다.
 * 그때 그냥 startActivity 를 부르면 ActivityNotFoundException 으로 앱이 죽으므로
 * 먼저 [Chucker.isOp] 로 진짜 라이브러리인지 확인한다.
 */
private fun openChucker(context: Context) {
    if (!Chucker.isOp) {
        Toast.makeText(context, "이 빌드에는 Chucker 가 없다", Toast.LENGTH_SHORT).show()
        return
    }
    val intent = Chucker.getLaunchIntent(context).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
