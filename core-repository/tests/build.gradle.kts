plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "core.repository.tests"
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
                implementation(libs.kotlinx.coroutines.test)

                implementation(projects.coreCommon)
                implementation(projects.coreData)
                implementation(projects.coreLocalization)
                implementation(projects.corePersistence)
                implementation(projects.coreRepository)
                implementation(kotlin("test-junit5"))
            }
        }
    }
}
