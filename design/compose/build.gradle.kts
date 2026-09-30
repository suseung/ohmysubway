plugins {
    alias(libs.plugins.ohmysubway.android.library)
}

android {
    namespace = "com.seungsu.ohmysubway.design.compose"
}

dependencies {
    api("com.suseung:designsystem:0.2.0")
}
