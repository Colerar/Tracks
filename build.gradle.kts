import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import java.time.ZoneOffset
import java.time.ZonedDateTime

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.gmazzo.buildconfig")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jlleitschuh.gradle.ktlint-idea")
    id("com.github.johnrengelman.shadow")
    application
}

group = "moe.sdl.tracks"
version = "1.4.2"

val commitHash by lazy {
    val commitHashCommand = "git rev-parse --short HEAD"
    Runtime.getRuntime().exec(commitHashCommand).inputStream.bufferedReader().readLine() ?: "UnkCommit"
}

val branch by lazy {
    val branchCommand = "git rev-parse --abbrev-ref HEAD"
    Runtime.getRuntime().exec(branchCommand).inputStream.bufferedReader().readLine() ?: "UnkBranch"
}

val time: Long by lazy {
    ZonedDateTime.now(ZoneOffset.UTC).toInstant().epochSecond
}

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
    // qrcode
    implementation("io.github.g0dkar:qrcode-kotlin-jvm:_")
    // cli
    implementation("com.github.ajalt.clikt:clikt:_")
    implementation("org.fusesource.jansi:jansi:_")
    implementation("me.tongfei:progressbar:_")
    implementation("dev.dirs:directories:_")
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

fun BuildConfigSourceSet.string(name: String, value: String) = buildConfigField("String", name, "\"$value\"")
fun BuildConfigSourceSet.stringNullable(name: String, value: String?) =
    buildConfigField("String?", name, value?.let { "\"$value\"" } ?: "null")

fun BuildConfigSourceSet.long(name: String, value: Long) = buildConfigField("long", name, value.toString())
fun BuildConfigSourceSet.longNullable(name: String, value: Long?) =
    buildConfigField("Long?", name, value?.let { "$value" } ?: "null")

buildConfig {
    packageName("$group.config")
    useKotlinOutput { topLevelConstants = true }
    string("VERSION", "$version")
    string("COMMIT_HASH", commitHash)
    string("BUILD_BRANCH", branch)
    long("BUILD_EPOCH_TIME", time)
}
