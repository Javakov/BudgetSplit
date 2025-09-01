plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "org.javakov.budgetsplit"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.javakov.budgetsplit"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    implementation (libs.androidx.appcompat.v161)
    implementation (libs.material.v1110)
    implementation (libs.androidx.constraintlayout)
    implementation (libs.androidx.lifecycle.livedata.ktx)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.navigation.fragment)
    implementation (libs.androidx.navigation.ui)
    
    // Room database
    implementation (libs.androidx.room.runtime)
    annotationProcessor (libs.androidx.room.compiler)
    
    // MPAndroidChart for pie chart
    implementation (libs.mpandroidchart)
    
    // Fragment support
    implementation (libs.androidx.fragment)
    
    testImplementation (libs.junit)
    androidTestImplementation (libs.androidx.junit.v115)
    androidTestImplementation (libs.androidx.espresso.core.v351)
}