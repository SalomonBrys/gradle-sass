package com.github.salomonbrys.gradle.sass

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import java.io.File

class SassExtension(private val project: Project) {

    val DEFAULT_VERSION = "1.14.2"
    val DEFAULT_SASS_EXE = if (OperatingSystem.current().isWindows) "sass.bat" else "sass"

    val DEFAULT_SASS_DIR = File("${project.gradle.gradleUserHomeDir}/sass")

    sealed class Exe {
        data class Download(var version: String, var outputDir: File) : Exe()
        data class Local(var path: String) : Exe()
    }

    var exe: Exe = Exe.Download(DEFAULT_VERSION, DEFAULT_SASS_DIR)

    @JvmOverloads
    fun download(action: Action<Exe.Download> = Action {}) {
        exe = Exe.Download(DEFAULT_VERSION, DEFAULT_SASS_DIR).also { action.execute(it) }
    }
    fun download(closure: Closure<*>) {
        exe = project.configure(Exe.Download(DEFAULT_VERSION, DEFAULT_SASS_DIR), closure) as Exe
    }

    @JvmOverloads
    fun local(action: Action<Exe.Local> = Action {}) {
        exe = Exe.Local(DEFAULT_SASS_EXE).also { action.execute(it) }
    }
    fun local(closure: Closure<*>) {
        exe = project.configure(Exe.Local(DEFAULT_SASS_EXE), closure) as Exe
    }
}
