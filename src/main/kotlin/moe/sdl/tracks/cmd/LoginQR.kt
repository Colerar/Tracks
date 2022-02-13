package moe.sdl.tracks.cmd

import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.coroutines.runBlocking
import moe.sdl.tracks.config.client
import moe.sdl.tracks.model.render
import moe.sdl.tracks.util.color
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.api.loginWebQRCodeInteractive

class LoginQR : CliktCommand(name = "loginqr", help = "扫码登录") {
    override fun run(): Unit = runBlocking {
        client.loginWebQRCodeInteractive()
        val info = client.getBasicInfo()
        if (info.data.isLogin) {
            echo("@|yellow,bold 登录成功!|@".color)
            echo(info.data.render())
        } else {
            echo("@|red 登录失败, 重试看看!|@".color)
        }
    }
}
