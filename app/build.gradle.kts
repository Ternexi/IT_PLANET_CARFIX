plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.carfixapplication"
    compileSdk = 34

    // ViewBinding должен быть здесь, и только здесь
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.carfixapplication"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Это важно для старых версий Android, чтобы они могли работать с векторной графикой
        vectorDrawables {
            useSupportLibrary = true
        }
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

    kotlinOptions {
        jvmTarget = "11"
        // Лишний блок buildFeatures отсюда УБРАН
    }
}

dependencies {
    // Зависимости для базовых компонентов Android
    implementation("androidx.core:core-ktx:1.12.0") // Обновил core-ktx для совместимости
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Вернул стабильную версию
    implementation("androidx.recyclerview:recyclerview:1.3.2") // Обновил recyclerview

    // Material Design - ОДИН РАЗ
    implementation("com.google.android.material:material:1.11.0")

    // Retrofit для сетевых запросов
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp для логирования и работы сети
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Корутины Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Тестовые зависимости
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
