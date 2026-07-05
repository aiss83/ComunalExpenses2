import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

fun runGit(command: List<String>): String {
    val result = providers.exec {
        commandLine(command)
        workingDir = rootProject.projectDir
        isIgnoreExitValue = true
    }
    return result.standardOutput.asText.get().trim()
}

val gitCommitCount: Int by lazy {
    runGit(listOf("git", "rev-list", "--count", "HEAD")).toIntOrNull() ?: 1
}

val gitVersionName: String by lazy {
    val count = gitCommitCount
    val tag = runGit(listOf("git", "describe", "--tags", "--abbrev=0")).takeIf { it.isNotEmpty() }
    if (tag != null) "$tag.$count" else "1.0.$count"
}

// Signing — read from keystore.properties (git-ignored)
fun readKeystoreProps(): Map<String, String> {
    val f = rootProject.file("keystore.properties")
    if (!f.exists()) return emptyMap()
    return f.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
        .associate { val (k, v) = it.split("=", limit = 2); k to v }
}

val keystoreProps = readKeystoreProps()

android {
    namespace = "ru.aiss83.comunalexpenses2.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.aiss83.comunalexpenses2"
        minSdk = 26
        targetSdk = 35
        versionCode = gitCommitCount
        versionName = gitVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProps["storeFile"] ?: return@create)
            storePassword = keystoreProps["storePassword"] ?: return@create
            keyAlias = keystoreProps["keyAlias"] ?: return@create
            keyPassword = keystoreProps["keyPassword"] ?: return@create
        }
    }

    buildTypes {
        release {
            signingConfig = if (keystoreProps.isNotEmpty()) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
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

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":shared"))

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
    implementation("androidx.activity:activity-compose:1.10.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
