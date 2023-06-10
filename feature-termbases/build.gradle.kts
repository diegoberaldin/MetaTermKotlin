plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "feature.termbases"
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

                implementation(compose.foundation)
                implementation(compose.animation)

                implementation(libs.koin)

                implementation(projects.coreCommon)
                implementation(projects.coreData)
                implementation(projects.corePersistence)
                implementation(projects.coreRepository)
                implementation(projects.coreLocalization)

                implementation(projects.featureTermbases.dialog.statistics)
                implementation(projects.featureTermbases.dialog.management)
                implementation(projects.featureTermbases.dialog.create)
                implementation(projects.featureTermbases.dialog.edit)

                implementation(projects.featureTermbases.wizard.metadata)
                implementation(projects.featureTermbases.wizard.definitionmodel)
                implementation(projects.featureTermbases.wizard.inputmodel)
            }
        }
        val jvmTest by getting
    }
}