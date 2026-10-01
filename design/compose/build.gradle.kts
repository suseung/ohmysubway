plugins {
    alias(libs.plugins.ohmysubway.android.library)
}

android {
    namespace = "com.seungsu.ohmysubway.design.compose"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "DESIGNSYSTEM_VERSION", "\"0.2.0\"")
    }
}

dependencies {
    api("com.suseung:designsystem:0.2.0")
}
