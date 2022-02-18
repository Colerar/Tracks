package moe.sdl.tracks.external.zhconvert

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.http.Parameters
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import moe.sdl.tracks.config.json
import moe.sdl.tracks.util.Log

suspend fun HttpClient.requestZhConvert(
    text: String,
    converter: Converter,
    context: CoroutineContext = Dispatchers.IO
): ConvertResponse = withContext(context) {
    Log.debug { "Getting text from zhconvert with converter $converter..." }
    post<String>(CONVERT) {
        body = FormDataContent(
            Parameters.build {
                append("text", text)
                append("converter", converter.code)
            }
        )
    }.let {
        json.decodeFromString<ConvertResponse>(it)
    }.also {
        Log.debug { "Received Response from zhconvert: ${it.code} - ${it.msg}" }
    }
}
