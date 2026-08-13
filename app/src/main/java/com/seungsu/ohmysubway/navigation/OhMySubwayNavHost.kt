package com.seungsu.ohmysubway.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.seungsu.ohmysubway.core.navigation.Screen
import com.seungsu.ohmysubway.guide.GuideScreen
import com.seungsu.ohmysubway.home.HomeScreen

@Composable
fun OhMySubwayNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home
    ) {
        composable<Screen.Home> {
            HomeScreen(onGuideClick = { navController.navigate(Screen.Guide) })
        }
        composable<Screen.Guide> {
            GuideScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
