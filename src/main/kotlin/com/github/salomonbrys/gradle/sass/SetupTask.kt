//package com.github.salomonbrys.gradle.sass
//
//import org.gradle.api.DefaultTask
//import org.gradle.api.artifacts.repositories.ArtifactRepository
//import org.gradle.api.artifacts.repositories.IvyArtifactRepository
//import org.gradle.api.artifacts.repositories.IvyPatternRepositoryLayout
//import org.gradle.api.tasks.Input
//import org.gradle.api.tasks.Internal
//import org.gradle.api.tasks.OutputDirectory
//import org.gradle.api.tasks.TaskAction
//import java.io.File
//import java.net.URI
//
//open class SetupTask : DefaultTask() {
//    private var repo: IvyArtifactRepository? = null
//
//    private val allRepos = ArrayList<ArtifactRepository>()
//
//    private val archive = "com.github.sass:dart-sass:1.14.2:linux-x64@tar.gz"
//
//w    @Input
//    fun getInput(): Set<String> {
//        val set = HashSet<String>()
//        set.add(archive)
//        return set
//    }
//
//    @OutputDirectory
//    fun getSassDir(): File =
//        project.file("${project.projectDir}/.gradle/sass/1.14.2")
//
//    @TaskAction fun exec() {
//        addRepository()
//        deleteExistingSass()
//        unpackSassArchive()
////        setExecutableFlag()
//        restoreRepositories()
//    }
//
//    private fun deleteExistingSass() {
//        this.project.delete(getSassDir().parent)
//    }
//
//    private fun unpackSassArchive() {
//        println("=====")
//        println(getSassArchiveFile().name)
//        println("=====")
//        if (getSassArchiveFile().name.endsWith("zip")) {
//            project.copy {
//                from(project.zipTree(getSassArchiveFile()))
//                into(getSassDir().parent)
//            }
//        } else {
//            project.copy {
//                from(project.tarTree(getSassArchiveFile()))
//                into(getSassDir().parent)
//            }
//            // Fix broken symlink
////            Path npm = Paths.get( variant.nodeBinDir.path, 'npm' )
////            if ( Files.deleteIfExists( npm ) )
////            {
////                Files.createSymbolicLink( npm, Paths.get( variant.npmScriptFile ) )
////            }
//        }
//    }
//
//    private fun setExecutableFlag() {
////        if ( !this.variant.windows )
////        {
////            File(this.variant.nodeExec).setExecutable(true)
////        }
//    }
//
////    @Internal
////    protected fun getSassExeFile(): File
////    {
////        return resolveSingle(dist)
////    }
//
//    @Internal
//    protected fun getSassArchiveFile(): File
//    {
//        return resolveSingle(archive)
//    }
//
//    private fun resolveSingle(name: String): File
//    {
//        val dep = this.project.dependencies.create(name)
//        val conf = this.project.configurations.detachedConfiguration(dep)
//        conf.isTransitive = false
//        return conf.resolve().iterator().next()
//    }
//
//    private fun addRepository() {
//        allRepos.clear()
//        allRepos.addAll(project.repositories)
//        project.repositories.clear()
//
//        val distUrl = "https://github.com/sass/dart-sass/releases/download"
//        repo = project.repositories.ivy {
//            url = URI(distUrl)
//            layout("pattern") {
//                this as IvyPatternRepositoryLayout
//                artifact("[revision]/[artifact](-[revision]-[classifier]).[ext]")
//            }
//        }
//    }
//
//    private fun restoreRepositories() {
//        project.repositories.clear()
//        project.repositories.addAll(this.allRepos)
//    }
//}
