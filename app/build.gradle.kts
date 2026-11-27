plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val useCloudFirebase = providers.gradleProperty("avaCloudFirebase")
    .map { it.toBooleanStrict() }
    .orElse(false)
    .get()
if (useCloudFirebase) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.antoniowalls.ava"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.antoniowalls.ava"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("boolean", "USE_CLOUD_FIREBASE", useCloudFirebase.toString())
        manifestPlaceholders["firebaseCleartext"] = (!useCloudFirebase).toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1" // Considera actualizar esto junto con tus versiones de Compose y Kotlin si encuentras problemas de compatibilidad.
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":lessons"))

    // --- Dependencias Base de Jetpack Compose (las que ya tenías) ---
    implementation("androidx.compose.ui:ui:1.5.1")
    implementation("androidx.compose.foundation:foundation:1.5.1")
    implementation("androidx.compose.material3:material3:1.1.0") // Material3
    implementation(libs.androidx.compose.material) // Esto parece ser Material (no M3), si usas M3 principalmente, podrías revisar si es necesaria.

    // --- Iconos de Material Design para Compose (Añadidos/Verificados) ---
    implementation("androidx.compose.material:material-icons-core:1.6.7") // O la última versión estable
    implementation("androidx.compose.material:material-icons-extended:1.6.7") // O la última versión estable

    // --- Firebase (Añadidos/Verificados) ---
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // BOM para manejar versiones de Firebase
    implementation(libs.firebase.firestore) // Firestore (ya la tenías)
    implementation(libs.firebase.auth)      // Firebase Auth (ya la tenías)
    implementation("com.google.firebase:firebase-storage-ktx")      // Firebase Storage (para subir imágenes de perfil)
    // --- Carga de Imágenes: Coil (la que ya tenías) ---
    implementation("io.coil-kt:coil-compose:2.6.0")

    // --- Otras dependencias de AndroidX y Jetpack (las que ya tenías)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose) // Jetpack Navigation para Compose

    // Para observar LiveData en Compose (las que ya tenías)
    implementation("androidx.compose.runtime:runtime-livedata:1.5.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1") // ViewModel y LiveData KTX

    // Material Design (no M3, la que ya tenías, revisa si es necesaria si migras todo a M3)
    implementation(libs.material)

    implementation("com.google.accompanist:accompanist-navigation-animation:0.32.0")


    // --- Pruebas (las que ya tenías) ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.1")
}
