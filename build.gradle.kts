import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    id("kotlin-platform-jvm")
    `kotlin-dsl`
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.9.10"
    id("com.jfrog.bintray") version "1.8.0"
}

group = "com.github.salomonbrys.gradle.sass"
version = "1.0.0"
description = "A Gradle plugin to download & run the official dart-sass release with Gradle"

repositories {
    jcenter()
    google()
    maven(url = "https://plugins.gradle.org/m2/")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
}

val kotlinVersion = "1.3.0-rc-146"

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("de.undercouch:gradle-download-task:3.4.3")
}

pluginBundle {
    website = "https://github.com/SalomonBrys/gradle-sass"
    vcsUrl = "https://github.com/SalomonBrys/gradle-sass.git"
    tags = listOf("sass", "css", "compiler", "web")

    plugins {
        create("Gradle-Sass") {
            id = "com.github.salomonbrys.gradle.sass"
            description = "A Gradle plugin to download & run the official dart-sass release with Gradle"
            displayName = "Gradle Sass"
        }
    }
}

publishing {
    (publications) {
        "GradleSass"(MavenPublication::class) {
            from(components["java"])
        }
    }
}
