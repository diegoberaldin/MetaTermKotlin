
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "feature.terms"
version = "1.0-SNAPSHOT"

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

                implementation(compose.foundation)
                implementation(compose.animation)

                implementation("moe.tlaster:precompose:1.3.14")
            }
        }
        val jvmTest by getting
    }
}

//compose.desktop {
//    application {
//        mainClass = "MainKt"
//        nativeDistributions {
//            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
//            packageName = "feature-terms"
//            packageVersion = "1.0.0"
//        }
//    }
//}
