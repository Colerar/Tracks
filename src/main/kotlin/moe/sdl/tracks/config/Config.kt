package moe.sdl.tracks.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.ProxyType
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.http
import io.ktor.client.features.UserAgent
import io.ktor.client.features.compression.ContentEncoding
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.protobuf.ProtoBuf
import moe.sdl.tracks.consts.JAR_DIR
import moe.sdl.tracks.consts.YABAPI_COOKIE_STORAGE_FILE
import moe.sdl.tracks.util.Log
import moe.sdl.yabapi.BiliClient
import moe.sdl.yabapi.Yabapi
import moe.sdl.yabapi.enums.LogLevel
import moe.sdl.yabapi.storage.FileCookieStorage
import okio.FileSystem
import okio.Path.Companion.toPath
import java.io.File

@Suppress("NOTHING_TO_INLINE")
private inline fun JsonBuilder.buildDefault() {
    isLenient = true
    coerceInputValues = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

internal val json by lazy {
    Json {
        buildDefault()
    }
}

internal val prettyPrintJson = Json {
    buildDefault()
    prettyPrint = true
}

@OptIn(ExperimentalSerializationApi::class)
internal val protoBuf = ProtoBuf

// Safari + MacOS User Agent
private const val WEB_USER_AGENT: String =
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 12_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.2 Safari/605.1.15"

internal val client by lazy {
    initYabapi()
    val httpClient = HttpClient(CIO) {
        engine {
            proxy = with(tracksPreference.proxy) {
                if (enable && type == ProxyType.HTTP && url != null) {
                    ProxyBuilder.http(url!!)
                } else null
            }.also {
                Log.debug { "Current proxy: $it, its raw config: ${tracksPreference.proxy}" }
            }
        }

        install(WebSockets) {
            this.pingInterval = 500
        }
        install(UserAgent) {
            agent = WEB_USER_AGENT
        }
        install(ContentEncoding) {
            gzip()
            deflate()
            identity()
        }
        install(HttpCookies) {
            storage = FileCookieStorage(FileSystem.SYSTEM, YABAPI_COOKIE_STORAGE_FILE.toPath())
        }
        defaultRequest {
            header(HttpHeaders.Accept, "*/*")
            header(HttpHeaders.AcceptCharset, "UTF-8")
        }
    }
    BiliClient(httpClient)
}

var isInitializedYabapi = false

var debug: Boolean = File(JAR_DIR, ".debug").exists()

internal fun initYabapi() = Yabapi.apply {
    if (!isInitializedYabapi) {
        defaultJson.getAndSet(json)
        yabapiLogLevel.getAndSet(
            if (debug) LogLevel.DEBUG else LogLevel.INFO
        )
        @Suppress("deprecation")
        log.getAndSet { tag, _, throwable, message ->
            val msg by lazy { message().replace("\r?\n".toRegex(), "↩") }
            Log.debug(throwable) { "$tag $msg" }
        }
        isInitializedYabapi = true
    }
}
