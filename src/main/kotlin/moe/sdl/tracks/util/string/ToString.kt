package moe.sdl.tracks.util.string

import kotlin.math.pow
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isDistantFuture
import kotlinx.datetime.toLocalDateTime

@Suppress("NOTHING_TO_INLINE")
internal inline fun Any?.toStringOrNull(): String? = this?.toString()

@Suppress("NOTHING_TO_INLINE")
internal inline fun Any?.toStringOrDefault(default: String = "--"): String = toStringOrNull() ?: default

internal inline fun <T> T?.toStringOrDefault(default: String = "--", transform: (raw: T) -> String): String =
    this?.let(transform) ?: default

/**
 * 将数字转换为带单位字符串
 * @param map 单位映射, 位数 to 单位, 需传入降序排列的 Map
 * @param precision 精度, 保留小数点后几位; 若无单位则无小数点, 有单位则会始终保留该精度
 * @return 形如 `1.2 万`, `5.1 亿` 的 [String]
 */
internal fun Int.toStringWithUnit(
    map: Map<UInt, String> = mapOf(
        9u to "亿",
        5u to "万",
        // 4 to "千",
    ),
    precision: Int = 1,
): String {
    if (this <= 0) return this.toString()
    map.forEach { (t, u) ->
        val i = 10.0.pow(t.toInt() - 1)
        if (this >= i) return String.format("%.${precision}f", this.toDouble() / i) + " " + u
    }
    return this.toString()
}

internal fun Int.secondsToDuration(): String {
    if (this <= 0) return "--"
    val seconds = this % 60
    val minutes = (this / 60) % 60
    val hours = this / 3600
    return if (hours >= 1) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Any.padZero(length: Int = 2) = this.toString().padStart(length, '0')

/**
 * UNIX 秒单位 时间戳 转 相对时间
 * 一分钟以内: 刚刚
 * 一小时以内: N 分钟前
 * 一天以内: N 小时前
 * 一周内: N 天前
 * 在同一年内: MM-dd
 * 不在同一年: yyyy-MM-dd
 */
internal fun Long.toRelativeTime(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val past = Instant.fromEpochSeconds(this)
    require(!past.isDistantFuture) { "Long.timestampToRelativeTime is only available for past Instant" }
    val now = Clock.System.now()
    val delta = now - past
    val nowDate by lazy { now.toLocalDateTime(timeZone) }
    val pastDate by lazy { past.toLocalDateTime(timeZone) }
    val monthAndDay by lazy {
        pastDate.date.monthNumber.padZero() + "-" +
                pastDate.date.dayOfMonth.padZero()
    }
    return when {
        delta < 1.toDuration(DurationUnit.MINUTES) -> "刚刚"
        delta < 1.toDuration(DurationUnit.HOURS) -> "${delta.inWholeMinutes} 分钟前"
        delta < 1.toDuration(DurationUnit.DAYS) -> "${delta.inWholeHours} 小时前"
        delta < 7.toDuration(DurationUnit.DAYS) -> "${delta.inWholeDays} 天前"
        nowDate.year == pastDate.year -> monthAndDay
        else -> "${pastDate.year}-$monthAndDay"
    }
}

internal fun Long.toAbsTime(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    val ldt = Instant.fromEpochSeconds(this).toLocalDateTime(timeZone)
    ldt.year.toString() + "-" + ldt.monthNumber.padZero() + "-" + ldt.dayOfMonth.padZero() + " " +
            ldt.hour.padZero() + ":" + ldt.minute.padZero() + ":" + ldt.second.padZero()
}
