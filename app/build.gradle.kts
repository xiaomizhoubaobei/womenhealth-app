import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

// ✅ AGP 8.5+ 新方式：kotlin 块在 android 块外
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// 读取签名配置 - 支持多层级配置：命令行参数 > 环境变量 > local.properties
val keystoreProperties = Properties()

// 第一优先级：从命令行参数获取
val storeFileParam = project.findProperty("KEYSTORE_FILE") as? String
val storePasswordParam = project.findProperty("KEYSTORE_PASSWORD") as? String
val keyAliasParam = project.findProperty("KEY_ALIAS") as? String
val keyPasswordParam = project.findProperty("KEY_PASSWORD") as? String

// 第二优先级：从环境变量获取
// 支持两种命名风格：CNB 风格（RELEASE_XXX）和标准风格（KEYSTORE_XXX）
val storeFileEnv = System.getenv("RELEASE_STORE_FILE") ?: System.getenv("KEYSTORE_FILE")
val storePasswordEnv = System.getenv("RELEASE_STORE_PASSWORD") ?: System.getenv("KEYSTORE_PASSWORD")
val keyAliasEnv = System.getenv("RELEASE_KEY_ALIAS") ?: System.getenv("KEY_ALIAS")
val keyPasswordEnv = System.getenv("RELEASE_KEY_PASSWORD") ?: System.getenv("KEY_PASSWORD")

// 第三优先级：从 local.properties 文件获取
val keystorePropertiesFile = rootProject.file("local.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

// 检测环境中是否存在密钥库密码和密钥密码
fun checkKeystoreCredentials(): Map<String, Boolean> {
    val credentials = mutableMapOf<String, Boolean>()

    // 检测密钥库密码（支持多种来源）
    credentials["keystorePassword"] = !storePasswordParam.isNullOrEmpty() ||
                                       !storePasswordEnv.isNullOrEmpty() ||
                                       keystoreProperties.containsKey("KEYSTORE_PASSWORD")

    // 检测密钥密码（支持多种来源）
    credentials["keyPassword"] = !keyPasswordParam.isNullOrEmpty() ||
                                  !keyPasswordEnv.isNullOrEmpty() ||
                                  keystoreProperties.containsKey("KEY_PASSWORD")

    return credentials
}

// 在配置阶段输出检测结果
val credentialsCheck = checkKeystoreCredentials()
println("=== Keystore Credentials Check ===")
println("KEYSTORE_PASSWORD available: ${credentialsCheck["keystorePassword"]}")
println("KEY_PASSWORD available: ${credentialsCheck["keyPassword"]}")
println("==================================")

android {
    namespace = "top.mizhoubaobei.womenhealth"
    compileSdk = 35

    defaultConfig {
        applicationId = "top.mizhoubaobei.womenhealth"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.0.1"
    }

    // 签名配置 - 支持多层级配置和 v1-v4 签名
    signingConfigs {
        create("release") {
            // 确定使用的配置文件路径（支持相对路径和绝对路径）
            // 默认从环境变量获取，如果没有则使用相对路径
            val defaultStoreFile = if (!storeFileEnv.isNullOrEmpty()) {
                file(storeFileEnv)
            } else {
                file("release.keystore")
            }
            
            // 按优先级选择配置源
            storeFile = when {
                !storeFileParam.isNullOrEmpty() -> file(storeFileParam)
                !storeFileEnv.isNullOrEmpty() -> file(storeFileEnv)
                keystoreProperties.containsKey("KEYSTORE_FILE") -> file(keystoreProperties["KEYSTORE_FILE"] as String)
                else -> defaultStoreFile
            }
            
            // 按优先级选择密码（带空值检查）
            storePassword = when {
                !storePasswordParam.isNullOrEmpty() -> storePasswordParam
                !storePasswordEnv.isNullOrEmpty() -> storePasswordEnv
                keystoreProperties.containsKey("KEYSTORE_PASSWORD") -> keystoreProperties["KEYSTORE_PASSWORD"] as String
                else -> throw GradleException("Keystore password not configured. Please set KEYSTORE_PASSWORD via: command line (-PKEYSTORE_PASSWORD=xxx), environment variable, or local.properties")
            }
            
            keyAlias = when {
                !keyAliasParam.isNullOrEmpty() -> keyAliasParam
                !keyAliasEnv.isNullOrEmpty() -> keyAliasEnv
                keystoreProperties.containsKey("KEY_ALIAS") -> keystoreProperties["KEY_ALIAS"] as String
                else -> throw GradleException("Key alias not configured. Please set KEY_ALIAS via: command line (-PKEY_ALIAS=xxx), environment variable, or local.properties")
            }
            
            keyPassword = when {
                !keyPasswordParam.isNullOrEmpty() -> keyPasswordParam
                !keyPasswordEnv.isNullOrEmpty() -> keyPasswordEnv
                keystoreProperties.containsKey("KEY_PASSWORD") -> keystoreProperties["KEY_PASSWORD"] as String
                else -> throw GradleException("Key password not configured. Please set KEY_PASSWORD via: command line (-PKEY_PASSWORD=xxx), environment variable, or local.properties")
            }
            
            // 启用 V1-V4 签名方案以获得最佳兼容性和安全性
            // V1: Jar signature (traditional) - 兼容 Android 5.0 以下
            // V2: APK Signature Scheme v2 (Android 7.0+) - 必需
            // V3: APK Signature Scheme v3 (Android 9.0+) - 推荐：支持密钥轮换
            // V4: APK Signature Scheme v4 (Android 11+) - 推荐：支持增量安装/ADB 快速部署
            // 由于 targetSdk=35 (Android 15)，强烈建议全部启用
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
            
            // 使用局部变量避免智能转换问题
            val storeFileLocal = storeFile
            val storePasswordLocal = storePassword
            val keyAliasLocal = keyAlias
            val keyPasswordLocal = keyPassword
            
            println("=== Signing Configuration ===")
            println("Store file: ${storeFileLocal?.absolutePath ?: "NOT SET"}")
            println("Store password: ${if (storePasswordLocal?.isNotEmpty() == true) "***" else "NOT SET"}")
            println("Key alias: $keyAliasLocal")
            println("Key password: ${if (keyPasswordLocal?.isNotEmpty() == true) "***" else "NOT SET"}")
            println("V1 signing: $enableV1Signing")
            println("V2 signing: $enableV2Signing")
            println("V3 signing: $enableV3Signing")
            println("V4 signing: $enableV4Signing")
            println("==============================")
        }
        
        // 修改默认的 debug 签名配置（Android Gradle 插件会自动创建 debug 配置）
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx.v1170)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.activity.ktx)
    implementation(libs.androidx.constraintlayout.v214)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx.v261)
    ksp(libs.androidx.room.compiler.v261)

    // ViewModel and LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx.v2100)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Chart Library (MPAndroidChart)
    implementation(libs.mpandroidchart)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Date and Time
    implementation(libs.threetenabp)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx.v282)
    implementation(libs.androidx.navigation.ui.ktx.v282)

    // RecyclerView
    implementation(libs.androidx.recyclerview)

    // CardView
    implementation(libs.cardview)
}