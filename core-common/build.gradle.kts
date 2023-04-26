plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "core.common"
version = libs.versions.appVersion.get()

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)

                implementation(libs.koin)
                implementation(libs.androidx.datastore)

                api(libs.bundles.log4j)
            }
        }
        val jvmTest by getting
    }
}