pluginManagement {    repositories {
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
        // 将高德仓库放在最前面，确保优先从这里下载
        maven { url = uri("https://amap-sdk.oss-cn-hangzhou.aliyuncs.com/maven") }

        google()
        mavenCentral()

        // 阿里云镜像
        maven { url = uri("https://maven.aliyun.com/repository/public") }

        // JitPack 仓库
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Travel"
include(":app")
