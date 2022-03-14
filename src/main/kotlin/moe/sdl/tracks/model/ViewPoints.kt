package moe.sdl.tracks.model

import moe.sdl.yabapi.data.video.ViewPoint

fun List<ViewPoint>.toMetadata(): String {
    val sb = StringBuilder()
    sb.appendLine(";FFMETADATA1")
    asSequence()
        .filter {
            it.from != null && it.to != null
        }
        .forEachIndexed { idx, it ->
            with(sb) {
                appendLine("[CHAPTER]")
                appendLine("TIMEBASE=1/1000")
                appendLine("START=${it.from!! * 1000}")
                appendLine("END=${it.to!! * 1000 - 1}")
                appendLine("title=${it.content ?: idx}")
                appendLine()
            }
        }
    return sb.toString()
}
