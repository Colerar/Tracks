package moe.sdl.tracks.util.encode

import com.soywiz.krypto.md5

object Md5 {
    fun digestBase64(data: String): String = data.toByteArray().md5().base64Url
}
