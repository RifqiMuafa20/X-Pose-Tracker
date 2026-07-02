import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

// --- Flex Delegate 16KB config ---
val flexDelegateRepo = "arxdeus/tflite_flex_16kb_android"
val flexDelegateTag = "latest"
val flexDelegateAbis = listOf("arm64-v8a", "armeabi-v7a", "x86_64")
val flexDelegateCacheDir = layout.buildDirectory.dir("flex-delegate")

val downloadFlexDelegate by tasks.registering {
    val cacheDir = flexDelegateCacheDir.get().asFile
    outputs.dir(cacheDir)

    onlyIf {
        flexDelegateAbis.any { abi ->
            !File(cacheDir, "$abi/libtensorflowlite_flex_jni.so").exists()
        }
    }

    doLast {
        val baseUrl = if (flexDelegateTag == "latest") {
            "https://github.com/$flexDelegateRepo/releases/latest/download"
        } else {
            "https://github.com/$flexDelegateRepo/releases/download/$flexDelegateTag"
        }

        flexDelegateAbis.forEach { abi ->
            val target = File(cacheDir, "$abi/libtensorflowlite_flex_jni.so")
            if (!target.exists()) {

                val url = "$baseUrl/libtensorflowlite_flex_jni.so-$abi"
                logger.lifecycle("Downloading flex delegate ($abi) from $url ...")
                target.parentFile.mkdirs()
                URI(url).toURL().openStream().use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                logger.lifecycle("Done: ${target.length() / 1_048_576} MB → $target")
            }
        }
    }
}

android {
    namespace = "com.rifqidev.x_posetracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rifqidev.x_posetracker"
        minSdk = 24
        targetSdk = 35
        versionCode = 9
        versionName = "2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += flexDelegateAbis
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
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
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
        mlModelBinding = true
    }

    androidResources {
        noCompress += "tflite"
    }

    sourceSets.getByName("main") {
        jniLibs.srcDir(flexDelegateCacheDir)
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
            pickFirsts += flexDelegateAbis.map { "lib/$it/libtensorflowlite_flex_jni.so" }
        }
    }
}

tasks.configureEach {
    if (name.startsWith("merge") && name.endsWith("NativeLibs")) {
        dependsOn(downloadFlexDelegate)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.runtime.saved.instance.state)
    implementation(libs.litert)
    implementation(libs.litert.api)
    implementation(libs.litert.support)
    implementation(libs.litert.metadata)
    implementation(libs.tensorflow.tensorflow.lite.select.tf.ops)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.mpandroidchart)
    implementation(libs.circleimageview)
    implementation(libs.androidx.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.glide)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.tasks.vision)
    implementation(libs.core)
    implementation(libs.androidx.camera.camera2.v133)
}