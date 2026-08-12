package com.seungsu.ohmysubway.core.base

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.seungsu.ohmysubway.core.mvi.MVIViewModel
import com.seungsu.ohmysubway.core.mvi.ViewEffect
import kotlinx.coroutines.launch

@Composable
fun CollectEffect(
    viewModel: MVIViewModel<*, *, *>,
    processEffect: (ViewEffect) -> Unit,
    onLoading: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        launch { viewModel.toastEffect.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
        launch { viewModel.effect.collect { processEffect(it) } }
        launch { viewModel.loadingEffect.collect { isLoading = it } }
    }
    if (isLoading) onLoading()
}
