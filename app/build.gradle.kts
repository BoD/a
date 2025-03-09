plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.sqldelight)
}

android {
    namespace = "org.jraf.android.a"

    compileSdk = 35

    defaultConfig {
        applicationId = "a.a.a.a"
        minSdk = 26
        targetSdk = 35
        versionCode = 25
        versionName = "1.16.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("SIGNING_STORE_PATH") ?: ".")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}

dependencies {
    implementation(libs.google.material)

    implementation(libs.timber)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)

    implementation(libs.sqldelight.androidDriver)
    implementation(libs.sqldelight.coroutines)

    implementation(libs.kprefs)
}

sqldelight {
    databases {
        create("Database") {
            schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
        }
    }
}

// To release:
// SIGNING_STORE_PATH=path/to/upload.keystore SIGNING_STORE_PASSWORD=password SIGNING_KEY_ALIAS=upload SIGNING_KEY_PASSWORD=password ./gradlew :app:bundleRelease

// `./gradlew :app:generateReleaseDatabaseSchema` to create the sqldelight database
// `./gradlew :app:verifySqlDelightMigration` to verify the migration
