package moe.sdl.tracks.external.zhconvert

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
abstract class AbstractResponse {
    @SerialName("code") abstract val code: ZhConvertCode
    @SerialName("msg") abstract val msg: String
    @SerialName("data") abstract val data: ResponseBody
    @SerialName("revisions") abstract val revisions: Revisions
    @SerialName("execTime") abstract val execTime: Double

    @Serializable
    data class Revisions(
        @SerialName("build") val build: String,
        @SerialName("msg") val msg: String,
        @SerialName("time") val time: Int,
    )

    inline fun onSuccess(action: (value: Int) -> Unit) = code.onSuccess(action)
    inline fun onFailed(action: (value: Int) -> Unit) = code.onFailed(action)
}

@JvmInline
@Serializable
value class ZhConvertCode(
    val value: Int
) {
    val isError: Boolean
        get() = value != 0

    val isSuccess: Boolean
        get() = value == 0

    inline fun onSuccess(action: (value: Int) -> Unit): ZhConvertCode {
        if (isSuccess) action(value)
        return this
    }

    inline fun onFailed(action: (value: Int) -> Unit): ZhConvertCode {
        if (isError) action(value)
        return this
    }
}

sealed interface ResponseBody
