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

                //implementation(libs.precompose)
                implementation("moe.tlaster:precompose:1.3.14")

                implementation(project(":core-common"))
                implementation(project(":core-data"))
                implementation(project(":core-persistence"))
                implementation(project(":core-repository"))
                implementation(project(":core-localization"))

                implementation(project(":feature-termbases:metadata"))
                implementation(project(":feature-termbases:definitionmodel"))
                implementation(project(":feature-termbases:inputmodel"))
            }
        }
        val jvmTest by getting
    }
}
