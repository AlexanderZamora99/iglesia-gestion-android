pluginManagement {
    repositories {
        google()
        // Mirror oficial de Google de Maven Central: evita los 403 de repo.maven.apache.org
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        // Mirror oficial de Google de Maven Central: evita los 403 de repo.maven.apache.org
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        mavenCentral()
    }
}

rootProject.name = "IglesiaGestion"
include(":app")
