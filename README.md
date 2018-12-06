Gradle Sass
===========

Gradle plugin to download and run Sass.
Compatible with Gradle continuous build.

This plugin is compatible with Kotlin Gradle DSL & offers the exact same API between Groovy & Kotlin DSLs.

**The Sass source files must, by default, be located in `src/main/sass`**.

## Install

```kotlin
plugins {
    id("com.github.salomonbrys.gradle.sass") version "1.0.1"
}
```

### Sass access configuration

#### Download

By default, the plugin downloads of [Dart-Sass](https://github.com/sass/dart-sass) (Sass official implementation).

If need be, you can configure the download:

```kotlin
sass {
    download {
        version = "1.13.4" // Default: "0.14.3".
        outputDir = file("wherever/I/want") // Default: "$gradleUserHome/sass".
    }
}
```

#### Local

You can instruct the plugin to run a locally installed version of sass instead of downloading a release:

```kotlin
sass {
    local()
}
```

You can configure the path to use:

```kotlin
sass {
    local {
        path = "/opt/dart-sass/sass" // Default: "sass".
    }
}
```

### Sass execution configuration

By default, the plugin creates a `sassCompile` task of type `SassTask`.

#### Output

You can configure the output directory:

```kotlin
sassCompile {
    outputDir = "web/css" // default: "$buildDir/sass".
}
```

#### Source map

You can configure the sourcemap generation to be in a file:

```kotlin
sassCompile {
    fileSourceMap() // This is the task's default
}
```

You can configure it:

```kotlin
sassCompile {
    fileSourceMap {
        embedSource = true // Default: false.
        url = absolute // Valid values: relative, absolute. Default: relative.
    }
}
```

You can configure the sourcemap generation to be embedded in the generated CSS:

```kotlin
sassCompile {
    embedSourceMap()
}
```

You can configure it:

```kotlin
sassCompile {
    embedSourceMap {
        embedSource = true // Default: false.
    }
}
```

You can configure no sourcemap generation at all:

```kotlin
sassCompile {
    noSourceMap()
}
```

#### Sources

`SassTask` tasks are `SourceTasks`, which means you can configure them just like any `SourceTask`:

```kotlin
sassCompile {
    source = fileTree("src/whynot/css")
    include { /*...*/ }
    exclude { /*...*/ }
}
```

#### Create new taks

You can easily create `SassTask` tasks:

Groovy DSL:

```groovy
task("mySassCompile", type: SassTask) {
    source = fileTree("src/main/my-sass")
}
```

Kotlin DSL:

```kotlin
task<SassTask>("mySassCompile") {
    source = fileTree("src/main/my-sass")
}
```
