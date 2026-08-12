package com.seungsu.ohmysubway.design.compose.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OhMySubwayBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    shape: Shape = OhMySubwayBottomSheetDefaults.ExpandedShape,
    containerColor: Color = OhMySubwayBottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = BottomSheetDefaults.Elevation,
    scrimColor: Color = OhMySubwayBottomSheetDefaults.ScrimColor,
    isRtl: Boolean = false,
    dragHandle: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val minHeight = LocalConfiguration.current.screenHeightDp * 0.25
    ModalBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier.heightIn(minHeight.dp),
        sheetState = sheetState, shape = shape, containerColor = containerColor, contentColor = contentColor,
        tonalElevation = tonalElevation, scrimColor = scrimColor, dragHandle = dragHandle
    ) {
        OhMySubwayTheme {
            CompositionLocalProvider(LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr) { content() }
        }
    }
}

object OhMySubwayBottomSheetDefaults {
    val ExpandedShape: Shape get() = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val ContainerColor: Color @Composable get() = OhMySubwayTheme.colors.background.groupedElevated
    val ScrimColor: Color @Composable get() = OhMySubwayTheme.colors.overlay.basic
}
