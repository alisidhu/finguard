plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.finguard.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.finguard.sample"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    lint {
        abortOnError = true
        warningsAsErrors = true
        disable.addAll(listOf("GradleDependency", "NewerVersionAvailable"))
    }
}

dependencies {
    implementation(project(":finguard-core"))
    implementation(project(":finguard-crypto"))
    implementation(project(":finguard-storage"))
    implementation(project(":finguard-network"))
    implementation(project(":finguard-auth"))
    implementation(project(":finguard-device"))
    implementation(project(":finguard-logging"))

    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
