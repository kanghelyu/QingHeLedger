pluginManagement {
    resolutionStrategy {
        eachPlugin {
            // 绕过没有及时同步的 KSP 插件标记包，
            // 直接下载真正的 KSP Gradle 插件。
            if (requested.id.id == "com.google.devtools.ksp") {
                useModule(
                    "com.google.devtools.ksp:symbol-processing-gradle-plugin:2.1.20-1.0.32"
                )
            }
        }
    }

    repositories {
        maven {
            url = uri("https://maven.aliyun.com/repository/central")
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
        }

        // 官方仓库作为备用
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        google()
        mavenCentral()
    }
}

rootProject.name = "QingHeLedger"
include(":app")
