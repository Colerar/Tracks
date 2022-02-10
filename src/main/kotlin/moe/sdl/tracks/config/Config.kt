package moe.sdl.tracks.config

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import moe.sdl.tracks.consts.YABAPI_COOKIE_STORAGE_FILE
import moe.sdl.tracks.util.Log
import moe.sdl.yabapi.BiliClient
import moe.sdl.yabapi.Yabapi
import moe.sdl.yabapi.consts.getDefaultHttpClient
import moe.sdl.yabapi.enums.LogLevel
import moe.sdl.yabapi.storage.FileCookieStorage
import okio.FileSystem
import okio.Path.Companion.toPath

internal val json by lazy {
    Json {
        prettyPrint = true
        isLenient = true
        coerceInputValues = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}

@OptIn(ExperimentalSerializationApi::class)
internal val protoBuf = ProtoBuf

internal val client by lazy {
    initYabapi()
    BiliClient(getDefaultHttpClient(FileCookieStorage(FileSystem.SYSTEM, YABAPI_COOKIE_STORAGE_FILE.toPath())))
}

var isInitializedYabapi = false

internal fun initYabapi() = Yabapi.apply {
    if (!isInitializedYabapi) {
        defaultJson.getAndSet(json)
        yabapiLogLevel.getAndSet(
            if (tracksPreference.isDebug) LogLevel.DEBUG else LogLevel.INFO
        )
        @Suppress("deprecation")
        log.getAndSet { tag, _, throwable, message ->
            val msg by lazy { message().replace("\n", "\\n") }
            Log.debug(throwable) { "$tag $msg" }
        }
        isInitializedYabapi = true
    }
}
