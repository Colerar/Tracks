package moe.sdl.tracks.model

import moe.sdl.tracks.util.color
import moe.sdl.yabapi.data.info.BasicInfoData
import org.fusesource.jansi.Ansi

fun BasicInfoData.render(): Ansi {
    return """
        @|cyan,bold =============== 当前帐号信息 ===============|@
        @|bold 用户名： $username|@
        @|bold 大会员： ${vip?.type.render()}|@
    """.trimIndent().color
}
