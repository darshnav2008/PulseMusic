plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.pulsemusic.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pulsemusic.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        // Paste your free Google Cloud YouTube Data API v3 Key here (optional until connecting YouTube)
        buildConfigField("String", "YOUTUBE_API_KEY", "\"YOUR_YOUTUBE_DATA_API_V3_KEY\"")
        // Paste your Web/Android OAuth Client ID from Google Cloud Console
        buildConfigField("String", "GOOGLE_OAUTH_CLIENT_ID", "\"YOUR_GOOGLE_OAUTH_CLIENT_ID\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Jetpack Compose (Apple Music-style 120Hz animations & UI)
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // AndroidX Media3 (ExoPlayer + Gapless Pre-buffering + Background Service)
    val media3Version = "1.4.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")
    implementation("androidx.media3:media3-datasource:$media3Version")
    implementation("androidx.media3:media3-datasource-okhttp:$media3Version")

    // Image Loading + Palette color extraction for dynamic Apple Music backgrounds
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.palette:palette-ktx:1.0.0")

    // Networking (Audius API + Official YouTube Data API v3)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Official Google Sign-In for YouTube Music / YouTube OAuth Connect
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Official YouTube IFrame Player wrapper for Android
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
