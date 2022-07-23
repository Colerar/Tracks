package moe.sdl.tracks.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import moe.sdl.yabapi.data.info.UserVip
import moe.sdl.yabapi.data.info.VipType

private const val NONE_VIP = "无大会员"

val UserVip?.renderedType: String
    get() {
        if (this == null) return NONE_VIP
        if (isShowSubscript == false) return NONE_VIP
        dueDate?.also {
            val expire = Instant.fromEpochSeconds(it)
            if (expire <= Clock.System.now()) return NONE_VIP
        }
        return when (type) {
            VipType.NONE -> NONE_VIP
            VipType.MONTH -> "月度大会员"
            VipType.YEAR -> "年度大会员"
            else -> "状态未知"
        }
    }
