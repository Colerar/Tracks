plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.5.31"
    application
}

group = "moe.sdl.tracks"
version = "1.1.0"

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "minutes")
}

application {
    mainClass.set("moe.sdl.tracks.TracksKt")
}

val ktorVersion = "1.6.7"

dependencies {
    testImplementation(kotlin("test"))
    // kotlinx
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
    implementation("org.jetbrains.kotlinx:atomicfu:0.17.0")
    // Ktor
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    // implementation("io.ktor:ktor-client-serialization:${Versions.ktor}")
    // implementation("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation("io.ktor:ktor-client-encoding:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    // bilibili api
    implementation("moe.sdl.yabapi:yabapi-core-jvm:0.0.9-SNAPSHOT")
    // file system
    implementation("com.squareup.okio:okio:3.0.0")
    // cli
    implementation("com.github.ajalt.clikt:clikt:3.4.0")
    implementation("org.fusesource.jansi:jansi:2.4.0")
    // FFmpeg cli wrapper
    implementation("net.bramp.ffmpeg:ffmpeg:0.6.2")
    // for turning off noisy log
    implementation("org.slf4j:slf4j-simple:1.7.36")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}


tasks.installDist {
    val map = listOf("./build/saved/config/", "./build/saved/storage/", "./build/saved/.debug").map {
        File(it) to File(it.replaceFirst(".", "./build/install/tracks/lib"))
    }
    doFirst {
        val dir = File("./build/saved/")
        if (dir.exists()) dir.deleteRecursively()
        map.forEach { (target, source) ->
            if (!source.exists()) return@forEach
            if (source.isDirectory) source.copyRecursively(target, true)
            if (source.isFile) source.copyTo(target, true)
        }
    }
    doLast {
        map.forEach { (source, target) ->
            if (!source.exists()) return@forEach
            if (source.isDirectory) source.copyRecursively(target, true)
            if (source.isFile) source.copyTo(target, true)
        }
    }
}
