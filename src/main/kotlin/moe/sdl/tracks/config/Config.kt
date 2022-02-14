package moe.sdl.tracks.config

import java.io.File
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.protobuf.ProtoBuf
import moe.sdl.tracks.consts.JAR_DIR
import moe.sdl.tracks.consts.YABAPI_COOKIE_STORAGE_FILE
import moe.sdl.tracks.util.Log
import moe.sdl.yabapi.BiliClient
import moe.sdl.yabapi.Yabapi
import moe.sdl.yabapi.consts.getDefaultHttpClient
import moe.sdl.yabapi.enums.LogLevel
import moe.sdl.yabapi.storage.FileCookieStorage
import okio.FileSystem
import okio.Path.Companion.toPath

@Suppress("NOTHING_TO_INLINE")
inline private fun JsonBuilder.buildDefault() {
    isLenient = true
    coerceInputValues = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

internal val json by lazy {
    Json {
        buildDefault()
    }
}

internal val prettyPrintJson = Json {
    buildDefault()
    prettyPrint = true
}

@OptIn(ExperimentalSerializationApi::class)
internal val protoBuf = ProtoBuf

internal val client by lazy {
    initYabapi()
    BiliClient(getDefaultHttpClient(FileCookieStorage(FileSystem.SYSTEM, YABAPI_COOKIE_STORAGE_FILE.toPath())))
}

var isInitializedYabapi = false

var debug: Boolean = File(JAR_DIR, ".debug").exists()

internal fun initYabapi() = Yabapi.apply {
    if (!isInitializedYabapi) {
        defaultJson.getAndSet(json)
        yabapiLogLevel.getAndSet(
            if (debug) LogLevel.DEBUG else LogLevel.INFO
        )
        @Suppress("deprecation")
        log.getAndSet { tag, _, throwable, message ->
            val msg by lazy { message().replace("\r?\n".toRegex(), "↩") }
            Log.debug(throwable) { "$tag $msg" }
        }
        isInitializedYabapi = true
    }
}
