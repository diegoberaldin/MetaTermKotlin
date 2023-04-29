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

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "MetaTerm"

include(
    ":core-common",
    ":core-data",
    ":core-persistence",
    ":core-repository",
    ":core-repository:repo",
    ":core-repository:usecase",
    ":core-localization",
    ":feature-base",
    ":feature-base:intro",
    ":feature-base:main",
    ":feature-terms",
    ":feature-terms:filter",
    ":feature-terms:detail",
    ":feature-terms:list",
    ":feature-termbases",
    ":feature-termbases:statistics",
    ":feature-termbases:management",
    ":feature-termbases:metadata",
    ":feature-termbases:definitionmodel",
    ":feature-termbases:inputmodel",
    ":feature-termbases:create",
    ":feature-termbases:edit",

    ":core-common:tests",
)
