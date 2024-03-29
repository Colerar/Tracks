package moe.sdl.tracks.model

import moe.sdl.tracks.consts.PART_SHOW_LIMIT
import moe.sdl.tracks.util.color
import moe.sdl.tracks.util.string.secondsToDuration
import moe.sdl.tracks.util.string.toStringOrDefault
import moe.sdl.yabapi.data.video.VideoPart
import org.fusesource.jansi.Ansi

fun List<VideoPart>.printConsole(showAll: Boolean = false) {
    println("@|bold 目标视频共有|@ @|yellow,bold $size|@ @|bold 个分 P|@".color)
    asSequence().filterIndexed { idx, _ ->
        idx <= PART_SHOW_LIMIT - 2 || idx == lastIndex || showAll
    }.forEach { part ->
        if (part.part == size.toLong() && part.part != 1L && !showAll) println(" ......")
        println("- ${part.toAnsi()}")
    }
}

fun VideoPart.toAnsi(): Ansi {
    val partPadded = part.toString().padStart(3, '0')
    val duration = duration?.toInt().toStringOrDefault { it.secondsToDuration() }
    return "P$partPadded - $name [$duration]".color
}
