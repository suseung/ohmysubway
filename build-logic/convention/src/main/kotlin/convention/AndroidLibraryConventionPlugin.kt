package convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(Plugins.AndroidLibrary)
                apply(Plugins.KotlinAndroid)
            }
            extensions.configure<LibraryExtension> {
                compileSdk = Configuration.compileSdk
                defaultConfig { minSdk = Configuration.minSdk }
                lint { disable += "NullSafeMutableLiveData" }
                compileOptions {
                    sourceCompatibility = Configuration.javaCompileTarget
                    targetCompatibility = Configuration.javaCompileTarget
                }
                packaging {
                    resources.excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
                }
            }
            configureKotlin()
        }
    }
}
