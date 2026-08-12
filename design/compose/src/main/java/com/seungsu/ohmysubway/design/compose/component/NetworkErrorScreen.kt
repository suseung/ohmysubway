package com.seungsu.ohmysubway.design.compose.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seungsu.ohmysubway.design.compose.ThemePreview
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme

@Composable
fun NetworkErrorScreen(title: String, description: String, retryText: String, modifier: Modifier = Modifier, onRetry: () -> Unit = {}) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        // TODO: 네트워크 에러 아이콘 drawable 추가 후 Image 컴포저블로 교체
        Text(title, fontWeight = FontWeight.Bold, color = OhMySubwayTheme.colors.label.onBgPrimary, textAlign = TextAlign.Center, style = OhMySubwayTheme.typos.regular.font18, modifier = Modifier.padding(top = 16.dp))
        Text(description, color = OhMySubwayTheme.colors.label.onBgPrimary, style = OhMySubwayTheme.typos.regular.font16, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = OhMySubwayTheme.colors.system.orange), shape = RoundedCornerShape(6.dp), modifier = Modifier.padding(top = 32.dp)) {
            Text(retryText, fontWeight = FontWeight.SemiBold, style = OhMySubwayTheme.typos.regular.font16, color = Color.White, modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp))
        }
    }
}

@ThemePreview
@Composable
private fun NetworkErrorScreenPreview() {
    OhMySubwayTheme {
        NetworkErrorScreen(
            title = "Network Error",
            description = "Please check your internet connection and try again.",
            retryText = "Retry"
        )
    }
}
