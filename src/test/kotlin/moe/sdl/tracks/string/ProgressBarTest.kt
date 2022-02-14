package moe.sdl.tracks.string

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.sdl.tracks.util.string.progressBar
import org.junit.jupiter.api.Test

class ProgressBarTest {
    @Test
    fun progressBarTest(): Unit = runBlocking {
        val cur = atomic(0L)
        val total = 1000L
        val job1 = launch {
            while (isActive) {
                cur.getAndAdd(3)
                delay(200L)
            }
        }
        val job2 = progressBar(cur, total)
        joinAll(job1, job2)
    }
}
