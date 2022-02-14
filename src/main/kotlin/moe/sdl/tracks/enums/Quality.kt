package moe.sdl.tracks.enums

import moe.sdl.yabapi.data.stream.QnQuality

val videoQualityMap by lazy {
    mapOf(
        "240p" to QnQuality.V240P,
        "360p" to QnQuality.V360P,
        "480p" to QnQuality.V480P,
        "720p" to QnQuality.V720P,
        "720p60f" to QnQuality.V720P60F,
        "1080p" to QnQuality.V1080P,
        "1080plus" to QnQuality.V1080Plus,
        "1080p60f" to QnQuality.V1080P60,
        "4k" to QnQuality.V4K,
        "hdr" to QnQuality.HDR,
        "dolby" to QnQuality.DOLBY,
        "8k" to QnQuality.V8K
    )
}

val audioQualityMap by lazy {
    mapOf(
        "low" to QnQuality.AUDIO_LOW,
        "medium" to QnQuality.AUDIO_MEDIUM,
        "high" to QnQuality.AUDIO_HIGH,
        "dolby" to QnQuality.AUDIO_DOLBY
    )
}
