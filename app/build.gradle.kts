import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.secrets)
}

android {
  namespace = "top.mizhoubaobei.womenhealth"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "top.mizhoubaobei.womenhealth"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "0.0.1"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    // GEMINI_API_KEY: read from environment variable (CI) or gradle property (local).
    // NOT managed by Secrets Gradle Plugin to avoid illegal Java expression
    // when the value is empty.
    val geminiApiKey = System.getenv("GEMINI_API_KEY")
        ?: (project.findProperty("GEMINI_API_KEY") as String?)
        ?: ""
    buildConfigField(
        "String",
        "GEMINI_API_KEY",
        "\"$geminiApiKey\""
    )
  }

  signingConfigs {
    create("release") {
      // 优先从密钥仓库导入的原始变量名读取（CI 环境，imports 直接注入）
      // 其次从 RELEASE_* 变量名读取（全局 env 映射后的变量）
      // 最后从 gradle.properties 读取（本地开发）
      val keystoreFilePath = System.getenv("RELEASE_STORE_FILE")
          ?: (project.findProperty("RELEASE_STORE_FILE") as String?)
      val keystorePwd = System.getenv("KEYSTORE_PASSWORD")
          ?: System.getenv("RELEASE_STORE_PASSWORD")
          ?: (project.findProperty("RELEASE_STORE_PASSWORD") as String?)
      val aliasName = System.getenv("KEY_ALIAS")
          ?: System.getenv("RELEASE_KEY_ALIAS")
          ?: (project.findProperty("RELEASE_KEY_ALIAS") as String?)
      val keyPwd = System.getenv("KEY_PASSWORD")
          ?: System.getenv("RELEASE_KEY_PASSWORD")
          ?: (project.findProperty("RELEASE_KEY_PASSWORD") as String?)

      if (keystoreFilePath != null && keystorePwd != null && aliasName != null && keyPwd != null) {
        storeFile = file(keystoreFilePath)
        storePassword = keystorePwd
        this.keyAlias = aliasName
        this.keyPassword = keyPwd
      }
      
      // 启用V1、V2、V3、V4签名
      enableV1Signing = (project.findProperty("ENABLE_V1_SIGNING") as String?)?.toBoolean() != false
      enableV2Signing = (project.findProperty("ENABLE_V2_SIGNING") as String?)?.toBoolean() != false
      enableV3Signing = (project.findProperty("ENABLE_V3_SIGNING") as String?)?.toBoolean() != false
      enableV4Signing = (project.findProperty("ENABLE_V4_SIGNING") as String?)?.toBoolean() != false
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
