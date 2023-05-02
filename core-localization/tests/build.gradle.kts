plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "core.localization.tests"
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

                implementation(project(":core-localization"))
                implementation(kotlin("test-junit5"))
            }
        }
    }
}
