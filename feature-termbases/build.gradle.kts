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

                implementation(project(":core-common"))
                implementation(project(":core-data"))
                implementation(project(":core-persistence"))
                implementation(project(":core-repository"))
                implementation(project(":core-localization"))

                api(project(":feature-termbases:statistics"))
                api(project(":feature-termbases:management"))
                api(project(":feature-termbases:create"))
                api(project(":feature-termbases:edit"))

                api(project(":feature-termbases:metadata"))
                api(project(":feature-termbases:definitionmodel"))
                api(project(":feature-termbases:inputmodel"))
            }
        }
        val jvmTest by getting
    }
}