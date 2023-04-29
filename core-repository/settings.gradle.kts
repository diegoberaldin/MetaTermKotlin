pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        kotlin("multiplatform").version(extra["kotlin.version"] as String)
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
    }
}

rootProject.name = "core-repository"

include(
    ":core-common",
    ":core-persistence",
    ":core-data",
    ":core-localization",
    ":repo",
)
project(":core-common").projectDir = File("../core-common")
project(":core-persistence").projectDir = File("../core-persistence")
project(":core-data").projectDir = File("../core-data")
project(":core-localization").projectDir = File("../core-localization")

project(":repo").projectDir = File("./repo")
