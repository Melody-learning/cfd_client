pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AstralW"

// ─── App Module ───
include(":app")

// ─── Core Modules ───
include(":core:core-common")
include(":core:core-ui")
include(":core:core-network")
include(":core:core-data")
include(":core:core-database")

// ─── Domain Modules ───
include(":domain:domain-math")

// ─── Feature Modules ───
include(":feature:feature-auth")
include(":feature:feature-market")
include(":feature:feature-chart")
include(":feature:feature-trading")
include(":feature:feature-portfolio")
include(":feature:feature-history")
include(":feature:feature-settings")
