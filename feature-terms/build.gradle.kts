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
                implementation(libs.decompose)
                implementation(libs.decompose.extensions)

                implementation(compose.foundation)
                implementation(compose.animation)

                implementation(libs.koin)

                implementation(projects.coreCommon)
                implementation(projects.coreData)
                implementation(projects.corePersistence)
                implementation(projects.coreRepository)
                implementation(projects.coreLocalization)

                implementation(projects.featureTerms.list)
                implementation(projects.featureTerms.detail)
                implementation(projects.featureTerms.dialog.filter)
            }
        }
        val jvmTest by getting
    }
}
