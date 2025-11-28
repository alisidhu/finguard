plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.finguard.sdk.demo"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
