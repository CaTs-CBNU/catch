
import java.util.Properties

val properties = Properties()
properties.load(project.rootProject.file("local.properties").inputStream())



plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id ("com.google.android.gms.oss-licenses-plugin")
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

apply(plugin = "com.google.android.gms.oss-licenses-plugin")

android {
    namespace = "com.cbnu.cat_ch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cbnu.cat_ch"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "GEMINI_API_KEY", properties.getProperty("GEMINI_API_KEY"))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true // Add this line to enable BuildConfig fields
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation(libs.androidx.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.github.rosenpin:fading-text-view:3.3")
    implementation ("com.ernestoyaquello.dragdropswiperecyclerview:drag-drop-swipe-recyclerview:1.2.0")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")
    implementation("com.github.mreram:showcaseview:1.4.1")
    implementation("com.github.rosenpin:fading-text-view:3.3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.1")
    kapt("androidx.room:room-compiler:2.6.1") // Ensure this line is included
    implementation(libs.lottie)
    implementation ("com.github.chnouman:AwesomeDialog:1.0.5")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.github.ibrahimsn98:SmoothBottomBar:1.7.9")
    implementation ("com.guolindev.permissionx:permissionx:1.8.1")
    implementation ("com.github.dhaval2404:imagepicker:2.1")
    implementation ("io.github.afreakyelf:Pdf-Viewer:2.1.1")
    implementation ("com.github.DImuthuUpe:AndroidPdfViewer:3.1.0-beta.1")
    implementation ("com.google.code.gson:gson:2.9.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1") // For logging network requests (optional)
    implementation ("com.github.acefalobi:android-stepper:0.3.0")
    implementation ("com.github.NitishGadangi:TypeWriter-TextView:v1.3")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation ("com.github.iamyashchouhan:AndroidPdfViewer:1.0.3")
    implementation ("com.github.skydoves:powerspinner:1.2.7")
    implementation ("com.google.android.gms:play-services-oss-licenses:17.0.0")

}