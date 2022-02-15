package moe.sdl.tracks.util.string

import kotlin.math.max
import kotlin.math.min
import kotlinx.atomicfu.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun CoroutineScope.progressBar(
    cur: AtomicLong,
    total: Long,
    freshInterval: Long = 100,
    length: Int = 50,
    mark: String = "#",
): Job = launch {
    var lastLen = 0
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
        str += String.format("%.1f%%", rate * 100)
        print(StringBuilder("\u0008").repeat(lastLen))
        print(str)
        lastLen = str.length
        if (rate >= 1) cancel("Download Complete")
        delay(freshInterval)
    }
}
