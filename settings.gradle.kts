import java.util.Properties

val localProperties = Properties().apply {
    file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
fun secret(key: String, envKey: String): String =
    localProperties.getProperty(key) ?: System.getenv(envKey) ?: ""

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackagesDesignSystem"
            url = uri("https://maven.pkg.github.com/suseung/ohmydesignsystem")
            credentials {
                username = secret("gpr.user", "GPR_USER")
                password = secret("gpr.key", "GITHUB_TOKEN")
            }
        }
        maven {
            name = "GitHubPackagesCore"
            url = uri("https://maven.pkg.github.com/suseung/ohmycore")
            credentials {
                username = secret("gpr.user", "GPR_USER")
                password = secret("gpr.key", "GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "ohmysubway"
include(":app")
include(":core")
include(":domain")
include(":data")
include(":design:compose")
include(":presentation:common")
include(":presentation:home")
include(":presentation:widget")
include(":presentation:guide")
