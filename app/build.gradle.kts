plugins {
    alias(libs.plugins.android.application)
    id("kotlin-parcelize")
    alias(libs.plugins.google.services)
    alias(libs.plugins.dagger)
    alias(libs.plugins.legacy.kapt)
    id("com.google.firebase.crashlytics")
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.androidx.navigation.safeargs)
}

android {
    namespace = "com.apiscall.skeletoncode"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }
    bundle {
        language {
            enableSplit = false
        }
    }

    defaultConfig {
        applicationId = "com.apiscall.skeletoncode"
        minSdk = 24
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

    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    flavorDimensions += "server"
    productFlavors.create("live") {
        dimension = "server"
        buildConfigField("String", "WebServiceUrl", "\"\"")
    }

    productFlavors.create("staging") {
        dimension = "server"
        buildConfigField("String", "WebServiceUrl", "\"\"")
    }

    productFlavors.create("local") {
        dimension = "server"
        buildConfigField("String", "WebServiceUrl", "\"\"") // local 2
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    /* Dagger Hilt */
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    /*ViewModel*/
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    /*Api call*/
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)

    /*RxJava & RxAndroid*/
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.retrofit2.rxjava2.adapter)

    //coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    /*Image loading*/
    implementation(libs.glide)
    kapt(libs.compiler)
    /*Firebase*/
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.messaging)

    /*Location*/
    implementation(libs.play.services.location)
    implementation(libs.play.services.places)
    implementation(libs.places)

    /*Room database components*/
    implementation(libs.androidx.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    androidTestImplementation(libs.room.testing)
    /*Paper Database*/
    implementation(libs.paperdb)

    /*Loader*/
    implementation(libs.avloadingindicatorview)/*lottieAnimation*/
    implementation(libs.lottie)

    //---OCR
    //noinspection OutdatedLibrary
    implementation(libs.firebase.ml.vision)

    implementation(libs.pinview)

    //-Richtext
    implementation(libs.richeditor.android)

    implementation(libs.viewpagerindicator)

    //------- File utils
    implementation(libs.commons.io)

    //------- Read More
    implementation(libs.readmoreoption)

    //--------- PDF viewer
    implementation(libs.android.pdf.viewer)

    implementation(libs.isoparser)

    //ssp
    implementation(libs.ssp.android)
    implementation(libs.sdp.android)

    //Switch button
    implementation(libs.tristatetogglebutton)

    implementation(libs.android.gif.drawable)

    implementation(libs.app.update)
    implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:24.0.1")
    implementation("com.google.firebase:firebase-firestore:26.1.1")
    implementation("com.google.firebase:firebase-storage:22.0.1")
    // Security
    implementation("androidx.security:security-crypto:1.1.0")
// QR Code
    implementation("com.google.zxing:core:3.5.4")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.7")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("com.google.android.material:material:1.13.0")

}