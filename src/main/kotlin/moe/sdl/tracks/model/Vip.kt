package moe.sdl.tracks.model

import moe.sdl.yabapi.data.info.VipType

fun VipType?.render(): String = when (this) {
    VipType.NONE -> "无大会员"
    VipType.MONTH -> "月度大会员"
    VipType.YEAR -> "年度大会员"
    else -> "状态未知"
}
