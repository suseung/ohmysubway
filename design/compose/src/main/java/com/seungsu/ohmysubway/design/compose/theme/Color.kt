package com.seungsu.ohmysubway.design.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/* Blue */
val Blue05 = Color(0xFFF0F6FF); val Blue10 = Color(0xFFBCDAFF); val Blue20 = Color(0xFF89BDFF)
val Blue30 = Color(0xFF5CA2FF); val Blue40 = Color(0xFF358BFA); val Blue50 = Color(0xFF1978F0)
val Blue60 = Color(0xFF0064E5); val Blue70 = Color(0xFF0459C8); val Blue80 = Color(0xFF094EA8)
val Blue90 = Color(0xFF0E4287)

/* Red */
val Red05 = Color(0xFFFFF3F3); val Red10 = Color(0xFFFFC5C1); val Red20 = Color(0xFFFF9690)
val Red30 = Color(0xFFFF6C64); val Red40 = Color(0xFFFB493F); val Red50 = Color(0xFFEB342A)
val Red60 = Color(0xFFD7261C); val Red70 = Color(0xFFBF1C13); val Red80 = Color(0xFFA4160E)
val Red90 = Color(0xFF87120C)

/* Orange */
val Orange05 = Color(0xFFFFEBEB); val Orange10 = Color(0xFFFED0CD); val Orange20 = Color(0xFFFDB8B0)
val Orange30 = Color(0xFFFCA595); val Orange40 = Color(0xFFFB947A); val Orange50 = Color(0xFFED7100)
val Orange60 = Color(0xFFEB4D1B); val Orange70 = Color(0xFFDD2A0A); val Orange80 = Color(0xFFB11002)
val Orange90 = Color(0xFF870000)

/* Yellow */
val Yellow05 = Color(0xFFFFF9E7); val Yellow10 = Color(0xFFFFF2CF); val Yellow20 = Color(0xFFFFE19C)
val Yellow30 = Color(0xFFFFCF6F); val Yellow40 = Color(0xFFFBBD48); val Yellow50 = Color(0xFFEAA92C)
val Yellow60 = Color(0xFFD69518); val Yellow70 = Color(0xFFBD800B); val Yellow80 = Color(0xFF9F6A04)
val Yellow90 = Color(0xFF7E5400)

/* Green */
val Green05 = Color(0xFFE6F5E5); val Green10 = Color(0xFFC5E6BF); val Green20 = Color(0xFF9ED695)
val Green30 = Color(0xFF75C769); val Green40 = Color(0xFF54BB47); val Green50 = Color(0xFF2EAF1D)
val Green60 = Color(0xFF22A012); val Green70 = Color(0xFF0E8E00); val Green80 = Color(0xFF007D00)
val Green90 = Color(0xFF005F00)

/* Grey */
val Grey05 = Color(0xFFF7F7F7); val Grey10 = Color(0xFFECECEC); val Grey20 = Color(0xFFDFDFDF)
val Grey30 = Color(0xFFCFCFCF); val Grey40 = Color(0xFFBCBCBC); val Grey50 = Color(0xFFA5A5A5)
val Grey60 = Color(0xFF8A8A8A); val Grey70 = Color(0xFF5A5A5A); val Grey80 = Color(0xFF333333)
val Grey90 = Color(0xFF1A1A1A)

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)

@Immutable
data class OhMySubwayColors(
    val system: SystemColors = SystemColors(),
    val background: BackgroundColors = BackgroundColors(),
    val overlay: OverlayColors = OverlayColors(),
    val label: LabelColors = LabelColors(),
    val state: StateColors = StateColors(),
    val separator: Color = Grey90.copy(alpha = .08f)
)

val LocalOhMySubwayColors = staticCompositionLocalOf { OhMySubwayColors() }

@Immutable
data class SystemColors(
    val blue: Color = Blue50, val red: Color = Red50, val orange: Color = Orange50,
    val yellow: Color = Yellow40, val green: Color = Green60,
    val white: Color = White, val black: Color = Black,
    val grey: Color = Grey50, val grey2: Color = Grey40, val grey3: Color = Grey30,
    val grey4: Color = Grey20, val grey5: Color = Grey10, val grey6: Color = Grey05
)

@Immutable
data class BackgroundColors(
    val defaultBase: Color = White, val defaultElevated: Color = White,
    val groupedBase: Color = Grey05, val groupedUpperBase: Color = White,
    val groupedElevated: Color = White
)

@Immutable
data class OverlayColors(
    val thick: Color = Grey80.copy(alpha = 0.9f),
    val basic: Color = Grey90.copy(alpha = 0.4f),
    val thin: Color = Grey70.copy(alpha = 0.05f)
)

@Immutable
data class LabelColors(
    val onBgPrimary: Color = Grey90,
    val onBgSecondary: Color = Grey90.copy(alpha = 0.57f),
    val onBgTertiary: Color = Grey90.copy(alpha = 0.29f),
    val onTintPrimary: Color = Grey05,
    val onTintSecondary: Color = Grey05.copy(alpha = 0.6f)
)

@Immutable
data class StateColors(
    val onBgHover: Color = Grey70.copy(alpha = 0.08f),
    val onBgFocus: Color = Grey70.copy(alpha = 0.12f)
)

val DarkOhMySubwayColors = OhMySubwayColors(
    system = SystemColors(grey = Grey40, grey2 = Grey50, grey3 = Grey60, grey4 = Grey70, grey5 = Grey80, grey6 = Grey90),
    background = BackgroundColors(defaultBase = Grey90, defaultElevated = Grey80, groupedBase = Black, groupedUpperBase = Grey90, groupedElevated = Grey80),
    overlay = OverlayColors(thick = Grey70.copy(alpha = 0.9f), basic = Grey90.copy(alpha = 0.6f), thin = Grey70.copy(alpha = 0.28f)),
    label = LabelColors(onBgPrimary = Grey05, onBgSecondary = Grey05.copy(alpha = 0.6f), onBgTertiary = Grey05.copy(alpha = 0.3f), onTintPrimary = Grey90, onTintSecondary = Grey90.copy(alpha = 0.57f)),
    separator = Grey05.copy(alpha = .10f)
)
