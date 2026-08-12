plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.kotlin.compose.compiler.extension)
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation("com.google.dagger:hilt-android-gradle-plugin:${libs.versions.hilt.get()}")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${libs.versions.ksp.get()}")
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "ohmysubway.android.application"
            implementationClass = "convention.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "ohmysubway.android.library"
            implementationClass = "convention.AndroidLibraryConventionPlugin"
        }
    }
}
