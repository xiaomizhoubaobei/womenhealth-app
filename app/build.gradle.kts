plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    id("com.google.gms.google-services")
}

android {
    namespace = "top.mizhoubaobei.womenhealth"
    compileSdk = 35

    defaultConfig {
        applicationId = "top.mizhoubaobei.womenhealth"
        minSdk = 30
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // 从gradle.properties读取签名配置
            val keystoreFile = project.findProperty("RELEASE_STORE_FILE") as String?
            val keystorePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String?
            val keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String?
            val keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String?
            
            if (keystoreFile != null && keystorePassword != null && keyAlias != null && keyPassword != null) {
                storeFile = file(keystoreFile)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
            
            // 启用V1、V2、V3、V4签名
            enableV1Signing = (project.findProperty("ENABLE_V1_SIGNING") as String?)?.toBoolean() != false
            enableV2Signing = (project.findProperty("ENABLE_V2_SIGNING") as String?)?.toBoolean() != false
            enableV3Signing = (project.findProperty("ENABLE_V3_SIGNING") as String?)?.toBoolean() != false
            enableV4Signing = (project.findProperty("ENABLE_V4_SIGNING") as String?)?.toBoolean() != false
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.play.services.safetynet)
    implementation(libs.androidx.credentials.play.services.auth)
    
    // Room数据库
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // JSON序列化
    implementation(libs.gson)
    // 核心库脱糖
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    // 谷歌Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    //腾讯云HTTPDNS
    implementation(libs.httpdns.sdk)
    //华为AppGallery Connect
    implementation(libs.agconnect.auth)
    implementation(libs.agconnect.remoteconfig)
    implementation(libs.agconnect.function)
    implementation(libs.agconnect.database)
    implementation(libs.agconnect.applinking)
    implementation(libs.agconnect.crash)
    implementation(libs.agconnect.appmessaging)
    //阿里云HTTPDNS
    implementation (libs.alicloud.android.httpdns)
}
