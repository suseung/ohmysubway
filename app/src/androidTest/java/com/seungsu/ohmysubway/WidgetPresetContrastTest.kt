package com.seungsu.ohmysubway

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.seungsu.ohmysubway.widget.WIDGET_BACKGROUND_PRESETS
import com.seungsu.ohmysubway.widget.WidgetAppearance
import com.seungsu.ohmysubway.widget.resolveColors
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.max
import kotlin.math.min

/**
 * 배경색 프리셋이 전부 읽히는지 지킨다.
 *
 * 노선 색은 대부분 중간 밝기라, 글자색을 밝기 경계값 하나로 정하면 대비가 3:1 아래로 떨어진다.
 * 프리셋을 추가할 때 이 테스트가 그걸 잡아준다.
 */
@RunWith(AndroidJUnit4::class)
class WidgetPresetContrastTest {

    @Test
    fun 모든_배경색_프리셋에서_글자가_읽힌다() {
        val failures = mutableListOf<String>()

        WIDGET_BACKGROUND_PRESETS.forEach { (name, argb) ->
            val background = Color(argb)
            val colors = WidgetAppearance(backgroundArgb = argb).resolveColors()

            listOf(
                "본문" to (background contrastWith colors.primaryText),
                "노선명" to (background contrastWith colors.accent),
            ).forEach { (label, ratio) ->
                if (ratio < MIN_TEXT_CONTRAST) {
                    failures += "$name $label ${"%.1f".format(ratio)}:1"
                }
            }

            // 보조 글자(갱신 시각)는 옅게 쓰는 게 의도라 본문보다 기준을 낮춘다
            val secondary = background contrastWith colors.secondaryText.flattenOver(background)
            if (secondary < MIN_SECONDARY_CONTRAST) {
                failures += "$name 보조 ${"%.1f".format(secondary)}:1"
            }
        }

        assertTrue(
            "대비가 기준에 못 미치는 프리셋: ${failures.joinToString(", ")}",
            failures.isEmpty(),
        )
    }

    /**
     * 기본값이 목록에 없으면 위젯을 새로 추가했을 때 아무 색도 선택되지 않은 채로 뜬다.
     * 프리셋을 정리할 때 같이 놓치기 쉬워 함께 지킨다.
     */
    @Test
    fun 기본_배경색은_프리셋에_들어있다() {
        val default = WidgetAppearance().backgroundArgb
        assertTrue(
            "기본 배경색 #%06X 가 프리셋에 없다".format(default and 0xFFFFFF),
            WIDGET_BACKGROUND_PRESETS.any { (_, argb) -> argb == default },
        )
    }

    /** 투명도를 배경 위에 합성해 실제로 보이는 색을 만든다. */
    private fun Color.flattenOver(background: Color): Color = Color(
        red = red * alpha + background.red * (1 - alpha),
        green = green * alpha + background.green * (1 - alpha),
        blue = blue * alpha + background.blue * (1 - alpha),
    )

    private infix fun Color.contrastWith(other: Color): Float {
        val a = luminance()
        val b = other.luminance()
        return (max(a, b) + 0.05f) / (min(a, b) + 0.05f)
    }

    companion object {
        /** 굵은 글씨 기준(WCAG 큰 글씨 3:1)보다 여유를 둔 값 */
        private const val MIN_TEXT_CONTRAST = 4.0f
        private const val MIN_SECONDARY_CONTRAST = 3.0f
    }
}
