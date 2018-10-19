package com.github.salomonbrys.gradle.sass

import de.undercouch.gradle.tasks.download.Download
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

    override fun apply(project: Project) {

        val config = SassExtension(project)
        project.extensions.add("sass", config)

        val sassCompile = project.task<SassTask>("sassCompile") {
            group = "build"
            source = project.fileTree("src/main/sass")
        }

        project.afterEvaluate {
            val exe = config.exe
            val (os, ext) = when {
                OperatingSystem.current().isLinux -> "linux" to ArchiveExt.TARGZ
                OperatingSystem.current().isMacOsX -> "macos" to ArchiveExt.TARGZ
                OperatingSystem.current().isWindows -> "windows" to ArchiveExt.ZIP
                else -> throw IllegalStateException("Unsupported operating system")
            }
            val arch = if ("64" in System.getProperty( "os.arch" )) "x64" else "ia32"
            if (exe is SassExtension.Exe.Download) {
                val sassDownload = project.task<Download>("sassDownload") {
                    group = "build setup"
                    val archive = "dart-sass-${exe.version}-$os-$arch.$ext"
                    val output = File("${project.gradle.gradleUserHomeDir}/sass/archive/$archive")
                    onlyIf { !output.exists() }
                    src("https://github.com/sass/dart-sass/releases/download/${exe.version}/$archive")
                    dest(output)
                    tempAndMove(true)
                }

                val sassExtract = project.task<Copy>("sassExtract") {
                    group = "build setup"
                    dependsOn(sassDownload)
                    from(when(ext) {
                        ArchiveExt.TARGZ -> project.tarTree(sassDownload.dest)
                        ArchiveExt.ZIP -> project.zipTree(sassDownload.dest)
                    })
                    into(exe.outputDir.resolve(exe.version))
                }

                sassCompile.dependsOn(sassExtract)
            }
        }

        project.tasks["build"].dependsOn(sassCompile)
    }

}
