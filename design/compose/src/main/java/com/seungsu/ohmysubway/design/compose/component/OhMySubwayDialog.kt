package com.seungsu.ohmysubway.design.compose.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seungsu.ohmysubway.design.compose.ThemePreview
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OhMySubwayDialog(
    modifier: Modifier = Modifier,
    title: String = "",
    message: String = "",
    confirmText: String = "",
    cancelText: String = "",
    isCancelable: Boolean = true,
    isConfirmEnabled: Boolean = true,
    onDismiss: () -> Unit = {},
    onClickCanceled: () -> Unit = {},
    onClickConfirmed: () -> Unit = {}
) {
    val density = LocalDensity.current
    val widthThreshold = remember { with(density) { 128.dp.toPx() } }
    var cancelButtonWidth by remember { mutableIntStateOf(0) }
    var confirmButtonWidth by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(dismissOnClickOutside = isCancelable, dismissOnBackPress = isCancelable)) {
        OhMySubwayTheme {
            Card(modifier = modifier.widthIn(min = 280.dp, max = 400.dp).padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = OhMySubwayTheme.colors.background.groupedElevated),
                shape = RoundedCornerShape(4.dp)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    if (title.isNotEmpty()) Text(title, style = OhMySubwayTheme.typos.bold.font16, color = OhMySubwayTheme.colors.label.onBgPrimary)
                    if (message.isNotEmpty()) Text(modifier = Modifier.fillMaxWidth(), text = message, style = OhMySubwayTheme.typos.regular.font14, color = OhMySubwayTheme.colors.label.onBgPrimary)
                    FlowRow(modifier = Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val isStacked = confirmButtonWidth > widthThreshold || cancelButtonWidth > widthThreshold
                        if (isStacked) {
                            DialogConfirmButton(Modifier.fillMaxWidth(), confirmText, isConfirmEnabled, onClickConfirmed, onDismiss) { confirmButtonWidth = it }
                            DialogCancelButton(Modifier.fillMaxWidth(), cancelText, onClickCanceled, onDismiss) { cancelButtonWidth = it }
                        } else {
                            DialogCancelButton(Modifier, cancelText, onClickCanceled, onDismiss) { cancelButtonWidth = it }
                            DialogConfirmButton(Modifier, confirmText, isConfirmEnabled, onClickConfirmed, onDismiss) { confirmButtonWidth = it }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogConfirmButton(modifier: Modifier = Modifier, text: String, enabled: Boolean, onConfirmed: () -> Unit, onDismiss: () -> Unit, onSetWidth: (Int) -> Unit) {
    OhMySubwayFilledButton(modifier = modifier.onGloballyPositioned { onSetWidth(it.size.width) }, onClick = { onConfirmed(); onDismiss() }, size = ButtonSize.S, enabled = enabled) {
        Text(text, style = OhMySubwayTheme.typos.bold.font16, color = OhMySubwayTheme.colors.system.white, textAlign = TextAlign.Center)
    }
}

@Composable
fun DialogCancelButton(modifier: Modifier = Modifier, text: String, onCanceled: () -> Unit, onDismiss: () -> Unit, onSetWidth: (Int) -> Unit) {
    if (text.isNotEmpty()) {
        OhMySubwayGhostButton(modifier = modifier.onGloballyPositioned { onSetWidth(it.size.width) }, onClick = { onCanceled(); onDismiss() }, size = ButtonSize.S) {
            Text(text, style = OhMySubwayTheme.typos.bold.font16, color = OhMySubwayTheme.colors.label.onBgPrimary)
        }
    }
}

@ThemePreview
@Composable
private fun OhMySubwayDialogPreview() {
    OhMySubwayTheme {
        OhMySubwayDialog(
            title = "Title",
            message = "Are you sure you want to proceed?",
            confirmText = "Confirm",
            cancelText = "Cancel"
        )
    }
}
