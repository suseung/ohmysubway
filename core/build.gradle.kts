plugins {
    alias(libs.plugins.ohmysubway.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.seungsu.ohmysubway.core"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "CORE_VERSION", "\"0.2.0\"")
    }
}

dependencies {
    api("com.suseung:core:0.2.0")
    implementation(libs.kotlinx.serialization.json)
}
