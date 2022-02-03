package moe.sdl.tracks.util

@Deprecated("This property is only for wrapper propose, do not use it for internal logging",
    ReplaceWith("private val logger = mu.KotlinLogging.logger {}"),
    DeprecationLevel.WARNING
)
internal val loggerWrapper = mu.KotlinLogging.logger {  }
