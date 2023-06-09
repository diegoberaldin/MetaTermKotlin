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
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
    ":feature-base:dialog:settings",
    ":feature-base:intro",
    ":feature-base:main",
    ":feature-terms",
    ":feature-terms:detail",
    ":feature-terms:dialog:filter",
    ":feature-terms:list",
    ":feature-termbases",
    ":feature-termbases:dialog:statistics",
    ":feature-termbases:dialog:management",
    ":feature-termbases:dialog:create",
    ":feature-termbases:dialog:edit",
    ":feature-termbases:wizard:metadata",
    ":feature-termbases:wizard:definitionmodel",
    ":feature-termbases:wizard:inputmodel",

    ":core-common:tests",
    ":core-localization:tests",
    ":core-persistence:tests",
    ":core-repository:tests",
)
