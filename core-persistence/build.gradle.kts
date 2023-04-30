plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "core.persistence"
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

                implementation(libs.h2database)
                implementation(libs.exposed.core)
                implementation(libs.exposed.dao)
                implementation(libs.exposed.jdbc)

                implementation(libs.koin)

                implementation(project(":core-common"))
                implementation(project(":core-data"))
            }
        }
    }
}
