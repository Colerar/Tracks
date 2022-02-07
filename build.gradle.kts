import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.1-rc2"
}

group = "moe.sdl.tracks"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "minutes")
}

val ktorVersion = "1.6.7"

dependencies {
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
    // kotlinx
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
    implementation("moe.sdl.yabapi:yabapi-core-jvm:0.0.8-SNAPSHOT")
    // file system
    implementation("com.squareup.okio:okio:3.0.0")
    implementation("com.dropbox.mobile.store:store4:4.0.4-KT15")
    // encodings
    implementation("com.soywiz.korlibs.krypto:krypto-jvm:2.4.12")
    // log
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("ch.qos.logback:logback-classic:1.2.10")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "15"
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

compose.desktop {
    application {
        mainClass = "moe.sdl.tracks.MainKt"
        nativeDistributions {
            modules("java.compiler", "java.instrument", "java.sql", "jdk.unsupported", "java.naming")

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "tracks"
            packageVersion = "1.0.0"

            val iconsRoot = project.file("./src/main/resources/icons")
            macOS {
                iconFile.set(iconsRoot.resolve("tracks.icns"))
            }
            windows {
                iconFile.set(iconsRoot.resolve("tracks.ico"))
                menuGroup = "Tracks"
                upgradeUuid = "d43056ef-5ba7-42c6-9627-dd6d5f69330d"
            }
            linux {
                iconFile.set(iconsRoot.resolve("tracks@250w.png"))
            }
        }
    }
}
