val gradleKotlinVersion = "1.2.61"

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "kotlin-platform-jvm" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:$gradleKotlinVersion")
            }
        }
    }

    repositories {
        maven(url = "http://dl.bintray.com/kotlin/kotlin-eap")
        jcenter()
        maven(url = "https://plugins.gradle.org/m2/")
    }
}

rootProject.name = "Gradle-Sass"
