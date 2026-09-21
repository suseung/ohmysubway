package com.seungsu.ohmysubway.widget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

/** 사용자가 고른 배경 위에서 잘 읽히도록 계산된 위젯 색 묶음. */
data class ResolvedWidgetColors(
    val background: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val accent: Color,
)

/**
 * 배경에 맞춰 글자색을 자동으로 정한다 — 사용자가 글자색을 고를 필요가 없게.
 *
 * 밝기 경계값 하나로 나누면 중간 밝기 배경에서 틀린다. 노선 색이 대부분 거기에 몰려 있어서
 * (2호선 0.285, 3호선 0.328 …) 흰 글씨가 붙고 대비가 3:1 아래로 떨어졌다.
 * 그래서 경계값 대신 흰 글씨와 검은 글씨 중 **실제로 대비가 더 좋은 쪽**을 고른다.
 */
fun WidgetAppearance.resolveColors(): ResolvedWidgetColors {
    val base = Color(backgroundArgb)
    val primary = if (base.contrastWith(TEXT_ON_DARK) >= base.contrastWith(TEXT_ON_LIGHT)) {
        TEXT_ON_DARK
    } else {
        TEXT_ON_LIGHT
    }
    // 노선명 강조색(파랑)은 배경이 파란 계열이면 묻힌다. 그럴 땐 본문색으로 돌아간다.
    val accentCandidate = if (primary == TEXT_ON_DARK) ACCENT_ON_DARK else ACCENT_ON_LIGHT

    return ResolvedWidgetColors(
        background = base.copy(alpha = backgroundAlpha.coerceIn(MIN_ALPHA, 1f)),
        primaryText = primary,
        // 고정 회색을 쓰면 중간 밝기 배경에서 묻힌다. 본문색을 옅게 써서 어떤 배경에서도 같게 보이게 한다.
        secondaryText = primary.copy(alpha = SECONDARY_TEXT_ALPHA),
        accent = if (base.contrastWith(accentCandidate) >= MIN_ACCENT_CONTRAST) accentCandidate else primary,
    )
}

/** WCAG 명암비. 1(같은 색) ~ 21(검정과 흰색). */
private fun Color.contrastWith(other: Color): Float {
    val a = luminance()
    val b = other.luminance()
    return (max(a, b) + 0.05f) / (min(a, b) + 0.05f)
}

/** 투명도를 너무 낮추면 글씨가 배경화면에 묻히므로 하한을 둔다. */
const val MIN_ALPHA = 0.15f

private const val SECONDARY_TEXT_ALPHA = 0.78f
private const val MIN_ACCENT_CONTRAST = 4.5f
private val TEXT_ON_LIGHT = Color(0xFF14171A)
private val ACCENT_ON_LIGHT = Color(0xFF0B5FD0)
private val TEXT_ON_DARK = Color(0xFFFFFFFF)
private val ACCENT_ON_DARK = Color(0xFF7FB8FF)

/**
 * 설정 화면에서 고를 수 있는 배경색 프리셋 — 서울 지하철 1~9호선 공식 노선색.
 *
 * 노선색은 원색 그대로 둔다. 어둡게 낮추면 색조가 붙어 있는 3·6·9호선이 서로 구분되지 않는다.
 * 대신 글자색이 배경에 맞춰 자동으로 정해져서 읽히는 데 문제가 없다(resolveColors 참고).
 */
val WIDGET_BACKGROUND_PRESETS: List<Pair<String, Int>> = listOf(
    "1호선" to 0xFF0052A4.toInt(),
    "2호선" to 0xFF00A84D.toInt(),
    "3호선" to 0xFFEF7C1C.toInt(),
    "4호선" to 0xFF00A5DE.toInt(),
    "5호선" to 0xFF996CAC.toInt(),
    "6호선" to 0xFFCD7C2F.toInt(),
    "7호선" to 0xFF747F00.toInt(),
    "8호선" to 0xFFE6186C.toInt(),
    "9호선" to 0xFFBB8336.toInt(),
)
