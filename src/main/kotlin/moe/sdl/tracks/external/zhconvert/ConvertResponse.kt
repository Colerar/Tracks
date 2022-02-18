package moe.sdl.tracks.external.zhconvert

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConvertResponse(
    @SerialName("code") override val code: ZhConvertCode,
    @SerialName("msg") override val msg: String,
    @SerialName("data") override val data: ConvertResult,
    @SerialName("revisions") override val revisions: Revisions,
    @SerialName("execTime") override val execTime: Double,
) : AbstractResponse()

@Serializable
data class ConvertResult(
    @SerialName("converter") val converter: Converter,
    @SerialName("text") val text: String,
    @SerialName("diff") val diff: String?,
    @SerialName("usedModules") val usedModules: List<String>,
    @SerialName("jpTextStyles") val jpTextStyles: List<String>,
    @SerialName("textFormat") val textFormat: String,
): ResponseBody
