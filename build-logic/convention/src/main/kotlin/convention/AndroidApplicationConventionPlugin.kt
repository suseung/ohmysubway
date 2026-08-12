package convention

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(Plugins.AndroidApplication)
                apply(Plugins.KotlinAndroid)
                apply(Plugins.KotlinCompose)
            }
            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.apply {
                    applicationId = Configuration.applicationId
                    targetSdk = Configuration.targetSdk
                    versionCode = Configuration.versionCode
                    versionName = Configuration.versionName
                }
            }
        }
    }
}
