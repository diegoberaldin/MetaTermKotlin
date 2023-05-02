plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "feature.terms"
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
                implementation(libs.essenty.instancekeeper)

                implementation(compose.foundation)
                implementation(compose.animation)

                implementation(libs.koin)

                implementation(project(":core-common"))
                implementation(project(":core-data"))
                implementation(project(":core-persistence"))
                implementation(project(":core-repository"))
                implementation(project(":core-localization"))

                api(project(":feature-terms:detail"))
                api(project(":feature-terms:list"))
                api(project(":feature-terms:filter"))
            }
        }
        val jvmTest by getting
    }
}
