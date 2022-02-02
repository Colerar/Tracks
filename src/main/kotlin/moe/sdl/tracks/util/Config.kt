package moe.sdl.tracks.util

import kotlinx.serialization.json.Json
import moe.sdl.tracks.TracksConfig
import moe.sdl.yabapi.Yabapi
import moe.sdl.yabapi.enums.LogLevel

internal fun initYabapi() = Yabapi.apply {
    defaultJson.getAndSet(
        Json {
            prettyPrint = true
            isLenient = true
            coerceInputValues = true
            ignoreUnknownKeys = true
        }
    )
    yabapiLogLevel.getAndSet(
        if (TracksConfig.isDebug) LogLevel.DEBUG else LogLevel.INFO
    )
    @Suppress("deprecation")
    log.getAndSet { tag, level, throwable, message ->
        when (level) {
            LogLevel.VERBOSE -> loggerWrapper.trace("$tag $message", throwable)
            LogLevel.DEBUG -> loggerWrapper.debug("$tag $message", throwable)
            LogLevel.INFO -> loggerWrapper.info("$tag $message", throwable)
            LogLevel.WARN -> loggerWrapper.warn("$tag $message", throwable)
            LogLevel.ERROR -> loggerWrapper.error("$tag $message", throwable)
            LogLevel.ASSERT -> loggerWrapper.error("----- ASSERT ERROR ----- $tag $message", throwable)
        }
    }
}
