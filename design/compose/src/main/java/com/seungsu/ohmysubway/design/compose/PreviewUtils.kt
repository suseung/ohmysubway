package com.seungsu.ohmysubway.design.compose

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(backgroundColor = 0x000000, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(backgroundColor = 0xffffff, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class ThemePreview
