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

rootProject.name = "MetaTerm"

include(
    "core-common",
    "core-data",
    "core-persistence",
    "core-repository",
    "core-localization",
    "filter",
    "detail",
    "list"
)
project(":core-common").projectDir = File("../core-common")
project(":core-data").projectDir = File("../core-data")
project(":core-persistence").projectDir = File("../core-persistence")
project(":core-repository").projectDir = File("../core-repository")
project(":core-localization").projectDir = File("../core-localization")

project(":filter").projectDir = File("./filter")
project(":detail").projectDir = File("./detail")
project(":list").projectDir = File("./list")
