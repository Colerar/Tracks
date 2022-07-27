package moe.sdl.tracks.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import io.github.g0dkar.qrcode.QRCode
import io.ktor.http.CookieEncoding
import io.ktor.http.Url
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import moe.sdl.tracks.config.client
import moe.sdl.tracks.config.cookies
import moe.sdl.tracks.model.render
import moe.sdl.tracks.util.color
import moe.sdl.tracks.util.errorExit
import moe.sdl.yabapi.BiliClient
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.api.getWebQRCode
import moe.sdl.yabapi.api.loginWebConsole
import moe.sdl.yabapi.api.loginWebQRCode
import moe.sdl.yabapi.api.loginWebQRCodeInteractive
import moe.sdl.yabapi.api.loginWebSMSConsole
import moe.sdl.yabapi.data.login.LoginWebQRCodeResponseCode
import moe.sdl.yabapi.util.string.cookieFromHeader
import java.io.ByteArrayOutputStream
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.WindowConstants
import kotlin.coroutines.CoroutineContext

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

    private val noGui by option("-no-gui", "-G", help = "扫码不使用GUI").flag()

    override fun run(): Unit = runBlocking {
        when (way) {
            Way.SMS -> client.loginWebSMSConsole()
            Way.QRCODE -> if (noGui) client.loginWebQRCodeInteractive() else client.loginQRTracks()
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

suspend fun BiliClient.loginQRTracks(
    context: CoroutineContext = this.context,
) =
    withContext(context) {
        val getQrResponse = getWebQRCode()

        val frame = ByteArrayOutputStream().use { o ->
            QRCode(getQrResponse.data?.url ?: error("Failed to get url"))
                .render(cellSize = 10, margin = 20)
                .writeImage(o)

            val imageIcon = ImageIcon(o.toByteArray())
            JFrame().apply {
                title = "请通过手机 B 站扫码并确认"
                defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
                setBounds(0, 0, 0, 0)
                add(JLabel(imageIcon))
                pack()
                setLocationRelativeTo(null)
                isAlwaysOnTop = true
                isVisible = true
            }
        }

        try {
            withTimeoutOrNull(120_000) {
                val loop = atomic(true)
                do {
                    loginWebQRCode(getQrResponse).also {
                        require((it.dataWhenSuccess == null) xor (it.dataWhenFailed == null)) {
                            "Invalid Response"
                        }
                        if (it.dataWhenSuccess != null) {
                            loop.getAndSet(false)
                            return@also
                        }
                        when (it.dataWhenFailed) {
                            LoginWebQRCodeResponseCode.NOT_SCAN -> {}
                            LoginWebQRCodeResponseCode.NOT_CONFIRM -> {}
                            LoginWebQRCodeResponseCode.KEY_EXPIRED -> cancel("QRCode Time Out")
                            else -> throw IllegalStateException("unexpected code, ${it.dataWhenFailed}")
                        }
                    }
                    delay(1_000)
                } while (loop.value)
            }
        } finally {
            frame.dispose()
        }
    }
