pluginManagement {
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
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "FinguardSdk"

include(":finguard-core")
project(":finguard-core").projectDir = file("app")

include(":finguard-crypto")
include(":finguard-storage")
include(":finguard-network")
include(":finguard-auth")
include(":finguard-device")
include(":finguard-logging")
include(":finguard-demo")
include(":sample-app")
