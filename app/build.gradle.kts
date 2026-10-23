plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.tracker.matrixtracker"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.tracker.matrixtracker"
        minSdk = 30
        targetSdk = 37
        versionCode = 10015
        versionName = "1.0.15"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val envKeystore = System.getenv("KEYSTORE_FILE")?.takeIf { it.isNotBlank() }
            val releaseKeystore = if (envKeystore != null) file(envKeystore) else file(System.getProperty("user.home") + "/.android/release.keystore")
            val debugKeystore = file(System.getProperty("user.home") + "/.android/debug.keystore")

            if (releaseKeystore.exists()) {
                storeFile = releaseKeystore
                storePassword = System.getenv("KEYSTORE_PASSWORD")?.takeIf { it.isNotBlank() } ?: "android"
                keyAlias = System.getenv("KEY_ALIAS")?.takeIf { it.isNotBlank() } ?: "matrixreleasekey"
                keyPassword = System.getenv("KEY_PASSWORD")?.takeIf { it.isNotBlank() } ?: "android"
            } else if (debugKeystore.exists()) {
                storeFile = debugKeystore
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val relConfig = signingConfigs.getByName("release")
            if (relConfig.storeFile?.exists() == true) {
                signingConfig = relConfig
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)

    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}