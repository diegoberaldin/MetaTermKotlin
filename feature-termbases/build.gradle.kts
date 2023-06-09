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

                api(projects.featureTermbases.dialog.statistics)
                api(projects.featureTermbases.dialog.management)
                api(projects.featureTermbases.dialog.create)
                api(projects.featureTermbases.dialog.edit)

                api(projects.featureTermbases.wizard.metadata)
                api(projects.featureTermbases.wizard.definitionmodel)
                api(projects.featureTermbases.wizard.inputmodel)
            }
        }
        val jvmTest by getting
    }
}