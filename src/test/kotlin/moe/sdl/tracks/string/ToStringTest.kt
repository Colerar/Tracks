package moe.sdl.tracks.string

import kotlinx.datetime.Clock
import moe.sdl.tracks.util.string.Bandwidth
import moe.sdl.tracks.util.string.secondsToDuration
import moe.sdl.tracks.util.string.toRelativeTime
import moe.sdl.tracks.util.string.toStringOrDefault
import moe.sdl.tracks.util.string.toStringOrNull
import moe.sdl.tracks.util.string.toStringWithUnit
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ToStringTest {
    @Test
    fun toStringOrNullTest() {
        val a: Int? = null
        assertEquals(null, a.toStringOrNull())
    }

    @Test
    fun toStringOrDefaultTest() {
        val a: Int? = null
        assertEquals("--", a.toStringOrDefault("--"))
    }

    @Test
    fun toStringOrDefaultWithTransformTest() {
        var array: IntArray? = intArrayOf(1, 2, 3, 4, 5)
        assertEquals(
            "[1, 2, 3, 4, 5]",
            array.toStringOrDefault {
                it.contentToString()
            }
        )
        array = null
        assertEquals("--", array.toStringOrDefault("--"))
    }

    @Test
    fun toStringWithUnitTest() {
        assertEquals("1.1 亿", 111110000.toStringWithUnit())
        assertEquals("11.5 万", 114514.toStringWithUnit())
        assertEquals("1.0 万", 10000.toStringWithUnit())
        assertEquals("114", 114.toStringWithUnit())
        assertEquals("1140", 1140.toStringWithUnit())
    }

    @Test
    fun secondsToDurationTest() {
        assertEquals("01:40", 100.secondsToDuration())
        assertEquals("89:11:11", 321_071.secondsToDuration())
    }

    @Test
    fun relativeTimeTest() {
        val now = Clock.System.now().epochSeconds
        generateSequence(2L) {
            if (it <= 134_000_000) it * 2L else it + 500_000
        }.take(100).map {
            now - it
        }.forEach {
            it.toRelativeTime().also(::println)
        }
    }

    @Test
    fun bandwidthToString() {
        generateSequence(1L) {
            it * 2L
        }.take(40).forEach {
            Bandwidth(it).toShow().also(::println)
            Bandwidth(it).toBytes().toShow().also(::println)
        }
    }
}
