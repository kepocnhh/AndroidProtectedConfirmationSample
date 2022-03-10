repositories {
    mavenCentral()
    google()
}

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 23
        targetSdk = compileSdk
        applicationId = "sp.apc.sample"
        versionCode = 1
        versionName = "0.0.$versionCode"
    }

    sourceSets["main"].java.srcDir("src/main/kotlin")

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".$name"
            versionNameSuffix = "-$name"
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.5"
    }
}

androidComponents.onVariants { variant ->
    val output = variant.outputs.single()
    check(output is com.android.build.api.variant.impl.VariantOutputImpl)
    output.outputFileName.set("APC-${output.versionCode.get()}-${variant.buildType!!}.apk")
    afterEvaluate {
        tasks.getByName<JavaCompile>("compile${variant.name.capitalize()}JavaWithJavac") {
            targetCompatibility = "1.8"
        }
        tasks.getByName<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compile${variant.name.capitalize()}Kotlin") {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.compose.foundation:foundation:1.0.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.1")
}
