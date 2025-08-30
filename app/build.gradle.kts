plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.obe_mngt_sys"
    compileSdk = 35  // Ensure compatibility

    defaultConfig {
        applicationId = "com.example.obe_mngt_sys"
        minSdk = 26
        targetSdk = 34  // Match with compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.tracing.perfetto.handshake)
    implementation (libs.material.v160)
    // Remove duplicate material implementations
    // implementation(libs.material.v180)  // Remove this
    // implementation (libs.material.v1110) // Remove this
    implementation (libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.fragment.ktx)
    implementation (libs.material) // or latest version
    implementation (libs.itextg)

    // Remove these duplicate implementations
    // implementation (libs.androidx.recyclerview.v131)
    // implementation(libs.androidx.appcompat.v161)
    // implementation(libs.androidx.fragment.ktx.v162)

    // If you're using DataStore, add it like this (if not already present)

    // OR if you're using the core version directly
    implementation(libs.androidx.datastore.core.android)
}