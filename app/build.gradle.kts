plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.astralw.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.astralw.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // ─── Internal Modules ───
    implementation(project(":core:core-common"))
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-network"))
    implementation(project(":core:core-data"))
    implementation(project(":core:core-database"))
    implementation(project(":domain:domain-math"))
    implementation(project(":feature:feature-auth"))
    implementation(project(":feature:feature-market"))
    implementation(project(":feature:feature-chart"))
    implementation(project(":feature:feature-trading"))
    implementation(project(":feature:feature-portfolio"))
    implementation(project(":feature:feature-account"))
    implementation(project(":feature:feature-watchlist"))

    // ─── Compose ───
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.bundles.compose.debug)

    // ─── Activity & Lifecycle ───
    implementation(libs.activity.compose)
    implementation(libs.bundles.lifecycle)

    // ─── Navigation ───
    implementation(libs.navigation.compose)

    // ─── Hilt ───
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // ─── AndroidX ───
    implementation(libs.androidx.core.ktx)

    // ─── Logging ───
    implementation(libs.timber)

    // ─── Testing ───
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.androidx)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}
