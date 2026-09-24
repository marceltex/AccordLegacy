// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    val agpVersion = "9.2.1"
    id("com.android.application") version agpVersion apply false
    id("com.android.built-in-kotlin") version agpVersion apply false
    id("com.android.library") version agpVersion apply false
    val kotlinVersion = "2.3.0"
    kotlin("plugin.parcelize") version kotlinVersion apply false
    id("com.google.devtools.ksp") version "2.3.12" apply false
}

tasks.withType(JavaCompile::class) {
    options.compilerArgs.add("-Xlint:all")
}
