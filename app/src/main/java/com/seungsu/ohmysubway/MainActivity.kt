package com.seungsu.ohmysubway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme
import com.seungsu.ohmysubway.navigation.OhMySubwayNavHost

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // super.onCreate 전에 불러야 한다. 이걸 켜야 창을 시스템이 줄이지 않고
        // 인셋을 정하는 주체가 Compose 하나로 모인다 — 두 주체가 각자 인셋을 더하면
        // 키보드가 올라왔을 때 하단 버튼이 기기마다 과하게 밀리거나 가려진다.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            OhMySubwayTheme {
                OhMySubwayNavHost()
            }
        }
    }
}
