package com.github.salomonbrys.gradle.sass

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import java.io.File

open class SassTask : SourceTask() {

    @OutputDirectory
    var outputDir = project.buildDir.resolve("sass")

    enum class Url {
        RELATIVE,
        ABSOLUTE
    }

    sealed class SourceMaps {
        abstract val embedSource: Boolean
        data class None(override var embedSource: Boolean = false) : SourceMaps()
        data class Embed(override var embedSource: Boolean = false) : SourceMaps()
        data class File(override var embedSource: Boolean = false, var url: Url = Url.RELATIVE) : SourceMaps() {
            val relative = Url.RELATIVE
            val absolute = Url.ABSOLUTE
        }
    }

    var sourceMaps: SourceMaps = SourceMaps.File()

    init {
        this.dependsOn(project.tasks["sassPrepare"])
    }

    fun noSourceMap() {
        sourceMaps = SourceMaps.None()
    }

    @JvmOverloads
    fun embedSourceMap(action: Action<SourceMaps.Embed> = Action {}) {
        sourceMaps = SourceMaps.Embed().apply(action)
    }

    fun embedSourceMap(action: SourceMaps.Embed.() -> Unit) {
        sourceMaps = SourceMaps.Embed().apply(action)
    }

    fun embedSourceMap(action: Closure<*>) {
        sourceMaps = SourceMaps.Embed().apply(action)
    }

    @JvmOverloads
    fun fileSourceMap(action: Action<SourceMaps.File> = Action {}) {
        sourceMaps = SourceMaps.File().apply(action)
    }

    fun fileSourceMap(action: SourceMaps.File.() -> Unit) {
        sourceMaps = SourceMaps.File().apply(action)
    }

    fun fileSourceMap(action: Closure<*>) {
        sourceMaps = SourceMaps.File().apply(action)
    }

    @TaskAction
    internal fun compileSass() {
        val ext = project.extensions["sass"] as SassExtension

        getSource().visit {
            if (isDirectory || name.startsWith("_"))
                return@visit

            project.exec {
                val exe = ext.exe
                executable = when (exe) {
                    is SassExtension.Exe.Local -> exe.path
                    is SassExtension.Exe.Download -> "${exe.outputDir.absolutePath}/${exe.version}/dart-sass/${ext.DEFAULT_SASS_EXE}"
                }
                val sm = sourceMaps
                args =
                        listOf(
                            file.absolutePath,
                            File("${outputDir.absolutePath}/${relativePath.parent.pathString}/${file.nameWithoutExtension}.css").absolutePath
                        ) +
                        when (sm) {
                            is SourceMaps.None -> listOf("--no-source-map")
                            is SourceMaps.Embed -> listOf("--embed-source-map")
                            is SourceMaps.File -> listOf("--source-map-urls", sm.url.name.toLowerCase())
                        } +
                        when (sm.embedSource) {
                            true -> listOf("--embed-sources")
                            false -> listOf("--no-embed-sources")
                        }
            }
        }

    }
}
