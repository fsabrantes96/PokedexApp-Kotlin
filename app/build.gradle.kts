// Adicione estas dependências ao seu build.gradle.kts (nível do módulo)
// Certifique-se de usar as versões mais recentes

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // Necessário para Glide
}

android {
    // ... sua configuração padrão (compileSdk, defaultConfig, etc.)
    namespace = "com.example.projetoventurus" // Certifique-se que seu namespace está aqui
    compileSdk = 34 // Exemplo, use a sua SDK

    defaultConfig {
        applicationId = "com.example.projetoventurus" // Certifique-se que seu ID está aqui
        minSdk = 24 // Exemplo
        targetSdk = 34 // Exemplo
        versionCode = 1
        versionName = "1.0"
        // ...
    }

    // Habilita o ViewBinding para facilitar a referência aos layouts
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    // Configuração de compilação do Kotlin
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ViewModel e LiveData (para MVVM)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.activity:activity-ktx:1.7.2") // Para o 'by viewModels()'

    // Retrofit (para chamadas de rede)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Glide (para carregar imagens)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation ("com.airbnb.android:lottie:6.4.0")
}