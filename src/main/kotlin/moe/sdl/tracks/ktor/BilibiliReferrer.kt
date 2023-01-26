package moe.sdl.tracks.ktor

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.http.HttpHeaders
import io.ktor.util.AttributeKey

object BilibiliReferrer : HttpClientPlugin<Unit, BilibiliReferrer> {
    override val key: AttributeKey<BilibiliReferrer> = AttributeKey("HeaderWithCond")

    override fun prepare(block: Unit.() -> Unit): BilibiliReferrer = BilibiliReferrer

    override fun install(plugin: BilibiliReferrer, scope: HttpClient) {
        scope.requestPipeline.intercept(HttpRequestPipeline.Before) {
            if (context.url.host.contains("bilibili.com")) {
                context.headers.appendMissing(HttpHeaders.Referrer, listOf("https://www.bilibili.com"))
            }
        }
    }
}
