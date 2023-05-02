import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "it.meta.term"
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

                implementation(libs.precompose)
                implementation(libs.koin)

                implementation(project(":core-common"))
                implementation(project(":core-data"))
                implementation(project(":core-localization"))
                implementation(project(":core-persistence"))
                implementation(project(":core-repository"))

                implementation(project(":feature-base"))
                implementation(project(":feature-terms"))
                implementation(project(":feature-termbases"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(project(":core-common:tests"))
                implementation(project(":core-localization:tests"))
                implementation(project(":core-persistence:tests"))
                implementation(project(":core-repository:tests"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MetaTermKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "MetaTerm"
            packageVersion = libs.versions.appVersion.get().substring(0, 5)
            version = libs.versions.buildNumber.get()
            includeAllModules = true
            macOS {
                iconFile.set(project.file("res/icon.icns"))
                setDockNameSameAsPackageName = true
            }
            windows {
                iconFile.set(project.file("res/icon.ico"))
            }
            linux {
                iconFile.set(project.file("res/icon.png"))
            }
        }
    }
}