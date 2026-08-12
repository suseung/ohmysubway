package com.seungsu.ohmysubway.design.compose.ext

import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.throttledClickable(intervalMs: Long = 500L, enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    clickable(enabled) {
        val now = System.currentTimeMillis()
        if (now - lastClickTime < intervalMs) return@clickable
        lastClickTime = now
        onClick()
    }
}

@Composable
fun rememberThrottledClick(intervalMs: Long = 500L, enabled: Boolean = true, onClick: () -> Unit): () -> Unit {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val currentOnClick = rememberUpdatedState(onClick)
    return remember(intervalMs, enabled) {
        {
            if (!enabled) return@remember
            val now = System.currentTimeMillis()
            if (now - lastClickTime < intervalMs) return@remember
            lastClickTime = now
            currentOnClick.value()
        }
    }
}
