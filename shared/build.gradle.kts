import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight")
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        // Apply linker opts at compilation level so they propagate through static framework
        iosTarget.compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-linker-options")
                    freeCompilerArgs.add("-lsqlite3")
                }
            }
        }

        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            binaryOption("bundleId", "ru.aiss83.comunalexpenses2.shared")
            freeCompilerArgs += listOf(
                "-Xbinary=bundleId=ru.aiss83.comunalexpenses2.shared",
                "-opt-in=kotlinx.cinterop.ExperimentalForeignApi"
            )
            linkerOpts("-lsqlite3")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

                // kotlinx-datetime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

                // SQLDelight
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
                implementation("app.cash.sqldelight:primitive-adapters:2.0.2")

                // Settings (kmp-settings)
                api("com.russhwolf:multiplatform-settings:1.3.0")

                // Lifecycle ViewModel (KMP)
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.8.3")

                // Navigation (Decompose)
                implementation("com.arkivanov.decompose:decompose:3.3.0")
                implementation("com.arkivanov.decompose:extensions-compose:3.3.0")

                // Koin DI
                api("io.insert-koin:koin-core:4.0.2")
                api("io.insert-koin:koin-compose:4.0.2")
                api("io.insert-koin:koin-compose-viewmodel-navigation:4.0.2")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.10.1")
                implementation("app.cash.sqldelight:android-driver:2.0.2")
                implementation("io.insert-koin:koin-android:3.5.6")
                implementation("io.insert-koin:koin-androidx-compose:3.5.6")
                implementation("io.insert-koin:koin-compose:4.0.2")
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation("app.cash.sqldelight:native-driver:2.0.2")
            }
        }
    }
}

android {
    namespace = "ru.aiss83.comunalexpenses2.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("ResourcesDatabase") {
            packageName.set("ru.aiss83.comunalexpenses2.data")
        }
    }
}
