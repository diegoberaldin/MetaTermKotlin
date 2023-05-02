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
                implementation(libs.kotlinx.coroutines.test)

                implementation(project(":core-common"))
                implementation(project(":core-data"))
                implementation(project(":core-persistence"))
                implementation(project(":core-repository"))
                implementation(kotlin("test-junit5"))
            }
        }
    }
}