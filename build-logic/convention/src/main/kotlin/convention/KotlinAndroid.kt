package convention

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinAndroid(extension: ApplicationExtension) {
    extension.apply {
        compileSdk = Configuration.compileSdk
        defaultConfig { minSdk = Configuration.minSdk }
        lint { disable += "NullSafeMutableLiveData" }
        compileOptions {
            sourceCompatibility = Configuration.javaCompileTarget
            targetCompatibility = Configuration.javaCompileTarget
        }
        buildFeatures {
            buildConfig = true
            compose = true
        }
    }
    configureKotlin()
}

internal fun Project.configureKotlinJvm() {
    extensions.getByType<JavaPluginExtension>().apply {
        sourceCompatibility = Configuration.javaCompileTarget
        targetCompatibility = Configuration.javaCompileTarget
    }
    configureKotlin()
}

fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(Configuration.javaCompileTarget.toString()))
            val warningsAsErrors: String? by project
            allWarningsAsErrors.set(warningsAsErrors.toBoolean())
            freeCompilerArgs.addAll(
                "-Xstring-concat=inline",
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                "-opt-in=androidx.lifecycle.compose.ExperimentalLifecycleComposeApi",
            )
        }
    }
}
