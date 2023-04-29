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
    "statistics",
    "management",
    "metadata",
    "definitionmodel",
    "inputmodel",
    "create",
    "edit",
)
project(":core-common").projectDir = File("../core-common")
project(":core-data").projectDir = File("../core-data")
project(":core-persistence").projectDir = File("../core-persistence")
project(":core-repository").projectDir = File("../core-repository")
project(":core-localization").projectDir = File("../core-localization")

project(":statistics").projectDir = File("./statistics")
project(":management").projectDir = File("./management")
project(":metadata").projectDir = File("./metadata")
project(":definitionmodel").projectDir = File("./definitionmodel")
project(":inputmodel").projectDir = File("./inputmodel")
project(":create").projectDir = File("./create")
project(":edit").projectDir = File("./edit")
