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
        relative,
        absolute
    }

    sealed class SourceMaps {
        abstract val embedSource: Boolean
        data class None(override var embedSource: Boolean = false) : SourceMaps()
        data class Embed(override var embedSource: Boolean = false) : SourceMaps()
        data class File(override var embedSource: Boolean = false, var url: Url = Url.relative) : SourceMaps()
    }

    var sourceMaps: SourceMaps = SourceMaps.File()

    fun noSourceMap() {
        sourceMaps = SourceMaps.None()
    }

    @JvmOverloads
    fun embedSourceMap(action: Action<SourceMaps.Embed> = Action {}) {
        sourceMaps = SourceMaps.Embed().also { action.execute(it) }
    }
    fun embedSourceMap(closure: Closure<*>) {
        sourceMaps = project.configure(SourceMaps.Embed(), closure) as SourceMaps
    }

    @JvmOverloads
    fun fileSourceMap(action: Action<SourceMaps.File> = Action {}) {
        sourceMaps = SourceMaps.File().also { action.execute(it) }
    }
    fun fileSourceMap(closure: Closure<*>) {
        sourceMaps = project.configure(SourceMaps.File(), closure) as SourceMaps
    }

    @TaskAction
    fun compileSass() {
        val exe = (project.extensions["sass"] as SassExtension).exe

        getSource().visit {
            if (isDirectory || name.startsWith("_"))
                return@visit

            project.exec {
                executable = when (exe) {
                    is SassExtension.Exe.Local -> exe.path
                    is SassExtension.Exe.Download -> "${exe.outputDir.absolutePath}/${exe.version}/dart-sass/sass"
                }
                val sm = sourceMaps
                args =
                        listOf(
                            file.absolutePath,
                            File(outputDir.absolutePath + "/" + relativePath.parent.pathString + "/" + file.nameWithoutExtension + ".css").absolutePath
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
