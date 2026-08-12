package com.seungsu.ohmysubway.widget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** 사용자가 고른 배경 위에서 잘 읽히도록 계산된 위젯 색 묶음. */
data class ResolvedWidgetColors(
    val background: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val accent: Color,
)

/** 배경 밝기에 따라 글자색을 자동으로 정한다 — 사용자가 글자색을 고를 필요가 없게. */
fun WidgetAppearance.resolveColors(): ResolvedWidgetColors {
    val base = Color(backgroundArgb)
    val onLight = base.luminance() > LIGHT_BACKGROUND_LUMINANCE

    return ResolvedWidgetColors(
        background = base.copy(alpha = backgroundAlpha.coerceIn(MIN_ALPHA, 1f)),
        primaryText = if (onLight) TEXT_ON_LIGHT else TEXT_ON_DARK,
        secondaryText = if (onLight) SUBTEXT_ON_LIGHT else SUBTEXT_ON_DARK,
        accent = if (onLight) ACCENT_ON_LIGHT else ACCENT_ON_DARK,
    )
}

/** 투명도를 너무 낮추면 글씨가 배경화면에 묻히므로 하한을 둔다. */
const val MIN_ALPHA = 0.15f

private const val LIGHT_BACKGROUND_LUMINANCE = 0.4f
private val TEXT_ON_LIGHT = Color(0xFF14171A)
private val SUBTEXT_ON_LIGHT = Color(0xFF5A6068)
private val ACCENT_ON_LIGHT = Color(0xFF0B5FD0)
private val TEXT_ON_DARK = Color(0xFFFFFFFF)
private val SUBTEXT_ON_DARK = Color(0xFFC7CDD4)
private val ACCENT_ON_DARK = Color(0xFF7FB8FF)

/** 설정 화면에서 고를 수 있는 배경색 프리셋. */
val WIDGET_BACKGROUND_PRESETS: List<Pair<String, Int>> = listOf(
    "검정" to 0xFF1A1A1A.toInt(),
    "남색" to 0xFF12294A.toInt(),
    "지하철 초록" to 0xFF12503A.toInt(),
    "자주" to 0xFF3A1436.toInt(),
    "흰색" to 0xFFFFFFFF.toInt(),
    "연회색" to 0xFFE8EAED.toInt(),
)
