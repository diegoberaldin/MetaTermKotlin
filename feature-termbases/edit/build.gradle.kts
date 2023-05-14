plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "feature.termbases.edit"
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
                implementation(libs.koin)
                implementation(libs.essenty.instancekeeper)

                implementation(projects.coreCommon)
                implementation(projects.coreData)
                implementation(projects.corePersistence)
                implementation(projects.coreRepository)
                implementation(projects.coreLocalization)

                implementation(projects.featureTermbases.metadata)
                implementation(projects.featureTermbases.definitionmodel)
                implementation(projects.featureTermbases.inputmodel)
            }
        }
        val jvmTest by getting
    }
}
