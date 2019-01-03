package com.github.salomonbrys.gradle.sass

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import java.io.File
import java.lang.IllegalStateException

class SassPlugin : Plugin<Project> {

    private enum class ArchiveExt(val ext: String) {
        ZIP("zip"),
        TARGZ("tar.gz");

        override fun toString() = ext
    }

    @Suppress("UnstableApiUsage")
    private fun Project.applyPlugin() {

        val sassPrepare = task<DefaultTask>("sassPrepare")

        val config = SassExtension(this)
        extensions.add("sass", config)

        afterEvaluate {
            val exe = config.exe
            val (os, ext) = when {
                OperatingSystem.current().isLinux -> "linux" to ArchiveExt.TARGZ
                OperatingSystem.current().isMacOsX -> "macos" to ArchiveExt.TARGZ
                OperatingSystem.current().isWindows -> "windows" to ArchiveExt.ZIP
                else -> throw IllegalStateException("Unsupported operating system")
            }
            val arch = if ("64" in System.getProperty( "os.arch" )) "x64" else "ia32"
            if (exe is SassExtension.Exe.Download) {
                val sassDownload = task<Download>("sassDownload") {
                    group = "build setup"
                    val archive = "dart-sass-${exe.version}-$os-$arch.$ext"
                    val output = File("${gradle.gradleUserHomeDir}/sass/archive/$archive")
                    onlyIf { !output.exists() }
                    src("https://github.com/sass/dart-sass/releases/download/${exe.version}/$archive")
                    dest(output)
                    tempAndMove(true)
                }

                val sassExtract = task<Copy>("sassExtract") {
                    group = "build setup"
                    dependsOn(sassDownload)
                    from(when(ext) {
                        ArchiveExt.TARGZ -> tarTree(sassDownload.dest)
                        ArchiveExt.ZIP -> zipTree(sassDownload.dest)
                    })
                    into(exe.outputDir.resolve(exe.version))
                }

                sassPrepare.dependsOn(sassExtract)
            }
        }

        val sassCompile = task<SassTask>("sassCompile") {
            group = "build"
            source = fileTree("src/main/sass")
        }

        tasks.maybeCreate("build").dependsOn(sassCompile)
    }

    override fun apply(project: Project) = project.applyPlugin()
}
