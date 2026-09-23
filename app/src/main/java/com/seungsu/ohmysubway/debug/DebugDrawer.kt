package com.seungsu.ohmysubway.debug

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.systemGestureExclusion
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
import com.seungsu.ohmysubway.core.debug.DebugPreferences
import kotlinx.coroutines.launch

/** 화면 오른쪽 끝에서 이만큼 안쪽까지가 드로어를 여는 손잡이다. */
private val EDGE_WIDTH = 32.dp

/**
 * 손잡이의 세로 길이. 화면 높이만큼 늘리지 않고 200dp 로 못 박는다.
 *
 * 오른쪽 가장자리는 시스템 뒤로가기 제스처 구역이라, 그냥 두면 여기서 쓸어도
 * 드로어 대신 앱이 종료된다. [Modifier.systemGestureExclusion] 으로 비켜달라고
 * 요청할 수 있는데 **한 모서리당 200dp 까지만 들어준다.** 더 길게 잡으면 시스템이
 * 임의로 잘라서, 손잡이 어디를 잡느냐에 따라 어떤 날은 열리고 어떤 날은 뒤로
 * 가는 앱이 된다. 그래서 처음부터 들어주는 만큼만 잡는다.
 */
private val HANDLE_HEIGHT = 200.dp

private val DRAWER_WIDTH = 300.dp

/**
 * 오른쪽에서 왼쪽으로 쓸면 열리는 디버그 서랍.
 *
 * 릴리즈 빌드에서는 [content] 를 그대로 내보내고 아무것도 감싸지 않는다.
 *
 * **왜 레이아웃 방향을 뒤집나.** ModalNavigationDrawer 는 언제나 시작 모서리에서
 * 나온다. 오른쪽에서 나오게 하는 옵션이 없어서, 드로어만 RTL 로 계산시키고 그 안의
 * 실제 내용은 다시 LTR 로 되돌려 놓는다. 내용까지 RTL 로 두면 앱 화면 전체가
 * 거울처럼 뒤집힌다.
 *
 * **왜 드로어 자체의 스와이프를 끄나.** gesturesEnabled 를 켜두면 드로어가 화면
 * 전체의 가로 드래그를 가져간다. 그러면 달력 좌우 넘기기나 리스트 가로 스크롤이
 * 디버그 빌드에서만 안 먹는다. 닫혀 있을 때는 오른쪽 가장자리 [EDGE_WIDTH] 띠만
 * 드래그를 보고, 열린 뒤에는 원래 제스처로 닫을 수 있게 다시 켠다.
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
                            .align(Alignment.CenterEnd)
                            .width(EDGE_WIDTH)
                            .height(HANDLE_HEIGHT)
                            .zIndex(1f)
                            .systemGestureExclusion()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    // 오른쪽 -> 왼쪽. 반대로 끌면 무시한다.
                                    if (dragAmount < 0) setDrawerOpen(true)
                                }
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
