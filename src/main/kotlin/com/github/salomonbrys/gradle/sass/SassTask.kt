package com.github.salomonbrys.gradle.sass

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.get
import java.io.File
import java.io.Serializable
import java.util.regex.Pattern

private val SASS_FILE_NAME_PATTERN: Pattern = Pattern.compile(".*\\.(scss|css|sass)")

open class SassTask : SourceTask() {

    @OutputDirectory
    var outputDir = project.buildDir.resolve("sass")

    enum class Url {
        RELATIVE,
        ABSOLUTE
    }

    enum class Style {
        EXPANDED,
        COMPRESSED
    }

    val expanded = Style.EXPANDED
    val compressed = Style.COMPRESSED

    sealed class SourceMaps : Serializable {
        abstract val embedSource: Boolean
        data class None(override var embedSource: Boolean = false) : SourceMaps()
        data class Embed(override var embedSource: Boolean = false) : SourceMaps()
        data class File(override var embedSource: Boolean = false, var url: Url = Url.RELATIVE) : SourceMaps() {
            val relative = Url.RELATIVE
            val absolute = Url.ABSOLUTE
        }
    }

    @Input
    var sourceMaps: SourceMaps = SourceMaps.File()

    @Input
    var style: Style = Style.EXPANDED

    var loadPaths: MutableList<File> = ArrayList()

    init {
        this.dependsOn(project.tasks["sassPrepare"])
    }

    @InputFiles
    @SkipWhenEmpty
    override fun getSource(): FileTree {
        val libs = project.files (loadPaths
                .map { project.fileTree(it) }
                .map { it.filter { file -> file.isFile && SASS_FILE_NAME_PATTERN.matcher(file.name).matches() } }
        )
        return project.files (super.getSource(), libs).asFileTree
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

    fun loadPath (files: Array<File>) {
        loadPaths.addAll(files)
    }

    @TaskAction
    internal fun compileSass() {
        val ext = project.extensions["sass"] as SassExtension

        super.getSource().visit {
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
                        } +
                        loadPaths.map {
                            "--load-path=$it"
                        } +
                        listOf("--style=${style.name.toLowerCase()}")
            }
        }

    }
}
