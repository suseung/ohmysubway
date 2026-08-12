package com.seungsu.ohmysubway.design.compose.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seungsu.ohmysubway.design.compose.ThemePreview
import com.seungsu.ohmysubway.design.compose.ext.rememberThrottledClick
import com.seungsu.ohmysubway.design.compose.theme.*

enum class ButtonSize { XS, S, M, L }

object OhMySubwayButtonDefaults {
    private const val DisabledAlpha = 0.4f

    fun contentPadding(size: ButtonSize) = PaddingValues(
        horizontal = when (size) { ButtonSize.XS -> Space12; ButtonSize.S -> Space16; else -> Space24 },
        vertical = when (size) { ButtonSize.XS -> Space5; ButtonSize.S -> Space9; ButtonSize.M -> Space13; ButtonSize.L -> Space17 }
    )
    fun shape(size: ButtonSize): Shape = RoundedCornerShape(if (size == ButtonSize.XS) 6.dp else 8.dp)
    fun minWidth(size: ButtonSize): Dp = when (size) { ButtonSize.XS -> MinWidthButtonXS; ButtonSize.S -> MinWidthButtonS; ButtonSize.M -> MinWidthButtonM; ButtonSize.L -> MinWidthButtonL }
    fun maxWidth(size: ButtonSize): Dp = when (size) { ButtonSize.XS -> MaxWidthButtonXS; ButtonSize.S -> MaxWidthButtonS; ButtonSize.M -> MaxWidthButtonM; ButtonSize.L -> MaxWidthButtonL }
    fun fontStyle(size: ButtonSize) = TextStyle(fontWeight = FontWeight.Bold, fontSize = if (size == ButtonSize.XS) 14.sp else 16.sp)

    @Composable fun filledColors(containerColor: Color = OhMySubwayTheme.colors.system.orange, contentColor: Color = OhMySubwayTheme.colors.system.white) = ButtonDefaults.buttonColors(
        containerColor = containerColor, contentColor = contentColor,
        disabledContainerColor = containerColor.copy(alpha = DisabledAlpha),
        disabledContentColor = contentColor.copy(alpha = DisabledAlpha))

    @Composable fun ghostColors() = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent, contentColor = OhMySubwayTheme.colors.system.orange,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = OhMySubwayTheme.colors.system.orange.copy(alpha = DisabledAlpha))

    @Composable fun outlinedBorder(enabled: Boolean) = BorderStroke(
        width = OutLinedButtonStroke,
        color = if (enabled) OhMySubwayTheme.colors.system.orange else OhMySubwayTheme.colors.system.orange.copy(alpha = DisabledAlpha))
}

@Composable
fun OhMySubwayFilledButton(onClick: () -> Unit, size: ButtonSize, modifier: Modifier = Modifier, enabled: Boolean = true, colors: ButtonColors = OhMySubwayButtonDefaults.filledColors(), content: @Composable RowScope.() -> Unit) {
    Button(onClick = rememberThrottledClick(enabled = enabled, onClick = onClick),
        modifier = modifier.widthIn(min = OhMySubwayButtonDefaults.minWidth(size), max = OhMySubwayButtonDefaults.maxWidth(size)),
        enabled = enabled, colors = colors, shape = OhMySubwayButtonDefaults.shape(size),
        contentPadding = OhMySubwayButtonDefaults.contentPadding(size)
    ) { ProvideTextStyle(OhMySubwayButtonDefaults.fontStyle(size)) { content() } }
}

@Composable
fun OhMySubwayGhostButton(onClick: () -> Unit, size: ButtonSize, modifier: Modifier = Modifier, enabled: Boolean = true, colors: ButtonColors = OhMySubwayButtonDefaults.ghostColors(), content: @Composable RowScope.() -> Unit) {
    Button(onClick = rememberThrottledClick(enabled = enabled, onClick = onClick),
        modifier = modifier.widthIn(min = OhMySubwayButtonDefaults.minWidth(size), max = OhMySubwayButtonDefaults.maxWidth(size)),
        enabled = enabled, colors = colors, shape = OhMySubwayButtonDefaults.shape(size),
        elevation = null, contentPadding = OhMySubwayButtonDefaults.contentPadding(size)
    ) { ProvideTextStyle(OhMySubwayButtonDefaults.fontStyle(size)) { content() } }
}

@Composable
fun OhMySubwayOutlinedButton(onClick: () -> Unit, size: ButtonSize, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) {
    OutlinedButton(onClick = onClick,
        modifier = modifier.widthIn(min = OhMySubwayButtonDefaults.minWidth(size), max = OhMySubwayButtonDefaults.maxWidth(size)),
        enabled = enabled, colors = OhMySubwayButtonDefaults.ghostColors(),
        border = OhMySubwayButtonDefaults.outlinedBorder(enabled), shape = OhMySubwayButtonDefaults.shape(size),
        contentPadding = OhMySubwayButtonDefaults.contentPadding(size)
    ) { ProvideTextStyle(OhMySubwayButtonDefaults.fontStyle(size)) { content() } }
}

@ThemePreview
@Composable
private fun OhMySubwayButtonPreview() {
    OhMySubwayTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
            OhMySubwayFilledButton(onClick = {}, size = ButtonSize.M) { Text("Filled") }
            OhMySubwayGhostButton(onClick = {}, size = ButtonSize.M) { Text("Ghost") }
            OhMySubwayOutlinedButton(onClick = {}, size = ButtonSize.M) { Text("Outlined") }
        }
    }
}
