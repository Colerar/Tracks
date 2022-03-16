plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jlleitschuh.gradle.ktlint-idea")
    id("com.github.johnrengelman.shadow")
    application
}

group = "moe.sdl.tracks"
version = "1.2.1"

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

dependencies {
    testImplementation(kotlin("test"))
    // kotlinx
    implementation("org.jetbrains.kotlin:kotlin-reflect:_")
    implementation(KotlinX.serialization.json)
    implementation(KotlinX.serialization.protobuf)
    implementation(KotlinX.coroutines.core)
    implementation(KotlinX.datetime)
    implementation("org.jetbrains.kotlinx:atomicfu:_")
    // Ktor
    implementation(Ktor.client.core)
    implementation(Ktor.client.encoding)
    implementation(Ktor.client.cio)
    // bilibili api
    implementation("moe.sdl.yabapi:yabapi-core-jvm:_")
    // file system
    implementation(Square.okio)
    // cli
    implementation("com.github.ajalt.clikt:clikt:_")
    implementation("org.fusesource.jansi:jansi:_")
    // FFmpeg cli wrapper
    implementation("net.bramp.ffmpeg:ffmpeg:_")
    // for turning off noisy log
    implementation("org.slf4j:slf4j-simple:_")
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
