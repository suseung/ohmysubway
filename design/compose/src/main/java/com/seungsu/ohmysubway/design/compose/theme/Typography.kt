package com.seungsu.ohmysubway.design.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.seungsu.ohmysubway.design.compose.R

private val PretendardFontFamily = FontFamily(Font(R.font.pretendard_regular, FontWeight.Normal))
private val BoldStyle = TextStyle(fontFamily = PretendardFontFamily, fontWeight = FontWeight.W600, letterSpacing = (-0.2).sp)
private val RegularStyle = TextStyle(fontFamily = PretendardFontFamily, fontWeight = FontWeight.W400, letterSpacing = (-0.2).sp)

@Immutable
data class OhMySubwayTypography(
    val bold: BoldTextStyle = BoldTextStyle(),
    val regular: RegularTextStyle = RegularTextStyle()
)

val LocalOhMySubwayTypography = staticCompositionLocalOf { OhMySubwayTypography() }

@Immutable
data class BoldTextStyle(
    val font48: TextStyle = BoldStyle.copy(fontSize = 48.sp, lineHeight = 64.sp),
    val font36: TextStyle = BoldStyle.copy(fontSize = 36.sp, lineHeight = 48.sp),
    val font28: TextStyle = BoldStyle.copy(fontSize = 28.sp, lineHeight = 38.sp),
    val font24: TextStyle = BoldStyle.copy(fontSize = 24.sp, lineHeight = 34.sp),
    val font20: TextStyle = BoldStyle.copy(fontSize = 20.sp, lineHeight = 28.sp),
    val font18: TextStyle = BoldStyle.copy(fontSize = 18.sp, lineHeight = 24.sp),
    val font16: TextStyle = BoldStyle.copy(fontSize = 16.sp, lineHeight = 22.sp),
    val font14: TextStyle = BoldStyle.copy(fontSize = 14.sp, lineHeight = 20.sp),
    val font12: TextStyle = BoldStyle.copy(fontSize = 12.sp, lineHeight = 18.sp),
    val font11: TextStyle = BoldStyle.copy(fontSize = 11.sp, lineHeight = 18.sp)
)

@Immutable
data class RegularTextStyle(
    val font48: TextStyle = RegularStyle.copy(fontSize = 48.sp, lineHeight = 64.sp),
    val font36: TextStyle = RegularStyle.copy(fontSize = 36.sp, lineHeight = 48.sp),
    val font28: TextStyle = RegularStyle.copy(fontSize = 28.sp, lineHeight = 38.sp),
    val font24: TextStyle = RegularStyle.copy(fontSize = 24.sp, lineHeight = 34.sp),
    val font20: TextStyle = RegularStyle.copy(fontSize = 20.sp, lineHeight = 28.sp),
    val font18: TextStyle = RegularStyle.copy(fontSize = 18.sp, lineHeight = 24.sp),
    val font16: TextStyle = RegularStyle.copy(fontSize = 16.sp, lineHeight = 22.sp),
    val font14: TextStyle = RegularStyle.copy(fontSize = 14.sp, lineHeight = 20.sp),
    val font12: TextStyle = RegularStyle.copy(fontSize = 12.sp, lineHeight = 18.sp),
    val font11: TextStyle = RegularStyle.copy(fontSize = 11.sp, lineHeight = 18.sp)
)
