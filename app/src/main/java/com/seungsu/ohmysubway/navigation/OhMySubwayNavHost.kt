package com.seungsu.ohmysubway.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.seungsu.ohmysubway.core.navigation.Screen
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme
import com.seungsu.ohmysubway.guide.GuideScreen
import com.seungsu.ohmysubway.home.HomeScreen

private const val TRANSITION_MS = 280

@Composable
fun OhMySubwayNavHost(
    navController: NavHostController = rememberNavController()
) {
    val spec = tween<IntOffset>(TRANSITION_MS)
    NavHost(
        navController = navController,
        startDestination = Screen.Home,
        // 전환 중 화면 사이로 윈도우 배경이 비쳐 번쩍이지 않도록 앱 배경을 깔아둔다.
        modifier = Modifier
            .fillMaxSize()
            .background(OhMySubwayTheme.colors.background.defaultBase),
        // 계층 이동은 가로 슬라이드. 페이드를 쓰지 않아 두 화면 모두 불투명하게 유지된다.
        enterTransition = { slideInHorizontally(spec) { it } },
        exitTransition = { slideOutHorizontally(spec) { -it / 4 } },
        popEnterTransition = { slideInHorizontally(spec) { -it / 4 } },
        popExitTransition = { slideOutHorizontally(spec) { it } }
    ) {
        composable<Screen.Home> {
            HomeScreen(onGuideClick = { navController.navigate(Screen.Guide) })
        }
        composable<Screen.Guide> {
            GuideScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
