package moe.sdl.tracks.util.string

import kotlinx.atomicfu.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.tongfei.progressbar.ProgressBar
import moe.sdl.tracks.config.tracksPreference
import kotlin.math.roundToLong

fun CoroutineScope.progressBar(
    task: String,
    cur: AtomicLong,
    total: Long,
    freshInterval: Long = tracksPreference.show.progressInterval,
): Job = launch {
    val start = Clock.System.now()
    ProgressBar(task, Size(total).mib.roundToLong()).use {
        while (isActive) {
            val rate = (cur.value.toDouble() / total.toDouble())
            val deltaDuration = Clock.System.now() - start
            val delta = deltaDuration.inWholeMilliseconds
            val avg = if (delta > 0) {
                Size(cur.value).toBandwidthMs(delta).toBytesBandwidth()
            } else BytesBandwidth(0)
            it.extraMessage = if (avg.kibPerS < 1) {
                "Loading..."
            } else avg.toShow()
            it.stepTo(Size(cur.value).mib.roundToLong())
            if (rate >= 1) cancel("Download Complete")
            delay(freshInterval)
        }
    }
}
