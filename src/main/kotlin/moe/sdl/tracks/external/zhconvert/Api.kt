package moe.sdl.tracks.external.zhconvert

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import moe.sdl.tracks.config.json
import moe.sdl.tracks.util.Log
import kotlin.coroutines.CoroutineContext

suspend fun HttpClient.requestZhConvert(
    text: String, converter: Converter, context: CoroutineContext = Dispatchers.IO
): ConvertResponse = withContext(context) {
    Log.debug { "Getting text from zhconvert with converter $converter..." }
    post(CONVERT) {
        setBody(FormDataContent(Parameters.build {
            append("text", text)
            append("converter", converter.code)
        }))
    }.bodyAsText().let {
        json.decodeFromString<ConvertResponse>(it)
    }.also {
        Log.debug { "Received Response from zhconvert: ${it.code} - ${it.msg}" }
    }
}
