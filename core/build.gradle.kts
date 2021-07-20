plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    defaultConfig {
        minSdk = 23
        targetSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    val lifecycle = "2.4.0-alpha02"
    val navVersion = "2.3.5"
    val lifecycleVersion = "2.4.0-alpha02"

    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("com.google.android.material:material:1.4.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle")

    api("androidx.navigation:navigation-fragment-ktx:$navVersion")
    api("androidx.recyclerview:recyclerview:1.2.1")

    // Lifecycles only (without ViewModel or LiveData)
    api("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    api("com.wdullaer:materialdatetimepicker:4.2.3")
}