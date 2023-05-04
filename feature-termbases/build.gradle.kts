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
                implementation(libs.essenty.instancekeeper)

                implementation(compose.foundation)
                implementation(compose.animation)

                implementation(libs.koin)

                implementation(projects.coreCommon)
                implementation(projects.coreData)
                implementation(projects.corePersistence)
                implementation(projects.coreRepository)
                implementation(projects.coreLocalization)

                api(projects.featureTermbases.statistics)
                api(projects.featureTermbases.management)
                api(projects.featureTermbases.create)
                api(projects.featureTermbases.edit)

                api(projects.featureTermbases.metadata)
                api(projects.featureTermbases.definitionmodel)
                api(projects.featureTermbases.inputmodel)
            }
        }
        val jvmTest by getting
    }
}