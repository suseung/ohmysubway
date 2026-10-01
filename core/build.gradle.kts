plugins {
    alias(libs.plugins.ohmysubway.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.seungsu.ohmysubway.core"
}

dependencies {
    api("com.suseung:core:0.2.0")
    implementation(libs.kotlinx.serialization.json)
}
