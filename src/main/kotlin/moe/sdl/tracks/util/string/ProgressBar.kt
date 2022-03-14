package moe.sdl.tracks.util.string

import kotlinx.atomicfu.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import moe.sdl.tracks.config.tracksPreference
import kotlin.math.max
import kotlin.math.min

fun CoroutineScope.progressBar(
    cur: AtomicLong,
    total: Long,
    freshInterval: Long = tracksPreference.show.progressInterval,
    length: Int = 50,
    mark: String = "#",
): Job = launch {
    var lastLen = 0
    val start = Clock.System.now()
    while (isActive) {
        val rate = (cur.value.toDouble() / total.toDouble())
        var str = ""
        val downloadLen = (length * rate).toInt()
        repeat(min(downloadLen, length)) {
            str += mark
        }
        str += " "
        repeat(max(length - downloadLen, 0)) {
            str += " "
        }
        str += String.format("%.1f%%", min(rate * 100, 100.0)).padEnd(7, ' ')
        val delta = (Clock.System.now() - start).inWholeMilliseconds
        val avg = if (delta > 0) Size(cur.value).toBandwidthMs(delta).toBytesBandwidth() else BytesBandwidth(0)
        str += "avg: " + avg.toShow().padEnd(10, ' ')
        val etaSeconds = if (avg.bytes > 0) (total - cur.value) / avg.bytes else 0
        str += " eta: " + etaSeconds.toInt().secondsToDuration().padEnd(5, ' ')
        print(StringBuilder("\u0008").repeat(lastLen))
        print(str)
        lastLen = str.length
        if (rate >= 1) cancel("Download Complete")
        delay(freshInterval)
    }
}
