plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "feature.terms.list"
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
                implementation(compose.materialIconsExtended)

                implementation(libs.precompose)

                implementation(project(":core-common"))
                implementation(project(":core-data"))
                implementation(project(":core-persistence"))
                implementation(project(":core-repository"))
                implementation(project(":core-localization"))
            }
        }
        val jvmTest by getting
    }
}
