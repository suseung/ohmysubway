import java.util.Properties

plugins {
    alias(libs.plugins.ohmysubway.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.seungsu.ohmysubway.data"
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        // 서울 열린데이터광장 실시간 지하철 인증키 — local.properties의 SEOUL_SUBWAY_API_KEY
        buildConfigField(
            "String",
            "SEOUL_SUBWAY_API_KEY",
            "\"${localProperties.getProperty("SEOUL_SUBWAY_API_KEY") ?: "sample"}\"",
        )
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.okhttp)
    // 인스펙터 UI 는 debug 에만. main 소스가 ChuckerInterceptor 를 참조하므로
    // compileOnly 가 아니라 release 에 같은 API 의 no-op 를 넣어야 한다.
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.no.op)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
