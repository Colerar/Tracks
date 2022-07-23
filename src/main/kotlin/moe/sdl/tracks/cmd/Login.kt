package moe.sdl.tracks.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import io.ktor.http.CookieEncoding
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import moe.sdl.tracks.config.client
import moe.sdl.tracks.config.cookies
import moe.sdl.tracks.model.render
import moe.sdl.tracks.util.color
import moe.sdl.tracks.util.errorExit
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.api.loginWebConsole
import moe.sdl.yabapi.api.loginWebQRCodeInteractive
import moe.sdl.yabapi.api.loginWebSMSConsole
import moe.sdl.yabapi.util.string.cookieFromHeader

class Login : CliktCommand(name = "login", help = "扫码登录") {

    private enum class Way {
        SMS, QRCODE, COOKIE, PASSWORD
    }

    private val way by option(help = "登录的方式，包括可以使用 [-sms, -qr, -cookie, -pwd]").switch(
        "-sms" to Way.SMS,
        "-qr" to Way.QRCODE,
        "-cookie" to Way.COOKIE,
        "-pwd" to Way.PASSWORD
    ).default(Way.QRCODE)

    override fun run(): Unit = runBlocking {
        when (way) {
            Way.SMS -> client.loginWebSMSConsole()
            Way.QRCODE -> client.loginWebQRCodeInteractive()
            Way.PASSWORD -> client.loginWebConsole()
            Way.COOKIE -> {
                val cookie = prompt("请输入 Cookie")
                if (cookie.isNullOrBlank()) {
                    errorExit { "未输入 Cookie" }
                }
                cookieFromHeader(cookie, encoding = CookieEncoding.RAW).forEach {
                    cookies.addCookie(Url("https://.bilibili.com"), it)
                }
            }
        }

        val info = client.getBasicInfo()
        if (info.data.isLogin) {
            echo("@|yellow,bold 登录成功!|@".color)
            echo(info.data.render())
        } else {
            echo("@|red 登录失败, 重试看看!|@".color)
        }
    }
}
