package moe.sdl.tracks.config

import kotlinx.serialization.json.Json
import moe.sdl.tracks.consts.YABAPI_COOKIE_STORAGE_FILE
import moe.sdl.tracks.util.loggerWrapper
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
        log.getAndSet { tag, level, throwable, message ->
            when (level) {
                LogLevel.VERBOSE -> loggerWrapper.trace("$tag ${message()}", throwable)
                LogLevel.DEBUG -> loggerWrapper.debug("$tag ${message()}", throwable)
                LogLevel.INFO -> loggerWrapper.info("$tag ${message()}", throwable)
                LogLevel.WARN -> loggerWrapper.warn("$tag ${message()}", throwable)
                LogLevel.ERROR -> loggerWrapper.error("$tag ${message()}", throwable)
                LogLevel.ASSERT -> loggerWrapper.error("----- ASSERT ERROR ----- $tag ${message()}", throwable)
            }
        }
        isInitializedYabapi = true
    }
}
