pluginManagement {
    repositories {
        maven { url = uri("https://artifact.bytedance.com/repository/Volcengine/") }
        maven { url = uri("https://artifact.bytedance.com/repository/byteX/") }
        maven { url = uri("https://developer.huawei.com/repo/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        maven { url = uri("https://mirrors.huaweicloud.com/repository/maven/") }
        maven { url = uri("https://mirrors.163.com/maven/repository/maven-public/") }
        maven { url = uri("https://maven.aliyun.com/nexus/content/repositories/releases/") }
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://artifact.bytedance.com/repository/Volcengine/") }
        maven { url = uri("https://artifact.bytedance.com/repository/byteX/") }
        maven { url = uri("https://developer.huawei.com/repo/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        maven { url = uri("https://mirrors.huaweicloud.com/repository/maven/") }
        maven { url = uri("https://mirrors.163.com/maven/repository/maven-public/") }
        maven { url = uri("https://maven.aliyun.com/nexus/content/repositories/releases/") }
        google()
        mavenCentral()
    }
}

rootProject.name = "womenhealth"
include(":app")