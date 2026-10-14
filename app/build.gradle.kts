plugins {
    id("com.android.application")
}

android {
    namespace = "io.github.eng_mohamedhosny.silentdialer"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.eng_mohamedhosny.silentdialer"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.4"
    }

    signingConfigs {
        create("releaseSign") {
            storeFile = file("release.jks")
            storePassword = "SilentDialer2026"
            keyAlias = "release"
            keyPassword = "SilentDialer2026"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("releaseSign")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
}
