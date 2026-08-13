plugins {
    alias(libs.plugins.ohmysubway.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.seungsu.ohmysubway.guide"
}

dependencies {
    implementation(project(":design:compose"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
