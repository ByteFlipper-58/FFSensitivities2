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
        // Add these new repositories
        maven { url = uri("https://android-sdk.is.com/") } // IronSource
        maven { url = uri("https://artifact.bytedance.com/repository/pangle") } // Pangle
        maven { url = uri("https://sdk.tapjoy.com/") } // Tapjoy
        maven { url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea") } // Mintegral
        maven { url = uri("https://cboost.jfrog.io/artifactory/chartboost-ads/") } // Chartboost
        maven { url = uri("https://dl.appnext.com/") } // AppNext
    }
}

rootProject.name = "FF Sensitivities"
include(":app")
