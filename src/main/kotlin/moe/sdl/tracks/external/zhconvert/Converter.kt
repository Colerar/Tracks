package moe.sdl.tracks.external.zhconvert

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * ISO 639-1 language code reference: https://www.loc.gov/standards/iso639-2/php/code_list.php
 * sub tag reference: http://www.iana.org/assignments/language-subtag-registry/language-subtag-registry
 * "und" for unclear source
 */
@Serializable(with = Converter.Companion::class)
enum class Converter {
    UNKNOWN {
        override val code: String = "Unknown"
        override val languageCode: String = "und"
        override val languageName: String = "不明"
    },
    SIMPLIFIED {
        override val code: String = "Simplified"
        override val languageCode: String = "zh-Hans"
        override val languageName: String = "简体中文"
    },
    TRADITIONAL {
        override val code: String = "Traditional"
        override val languageCode: String = "zh-Hant"
        override val languageName: String = "繁体中文"
    },
    CHINA {
        override val code: String = "China"
        override val languageCode: String = "zh-CN"
        override val languageName: String = "大陆简中"
    },
    HONG_KONG {
        override val code: String = "Hongkong"
        override val languageCode: String = "zh-HK"
        override val languageName: String = "香港繁中"
    },
    TAI_WAN {
        override val code: String = "Taiwan"
        override val languageCode: String = "zh-TW"
        override val languageName: String = "台湾繁中"
    },
    PIN_YIN {
        override val code: String = "Pinyin"
        override val languageCode: String = "zh-Latn-pinyin"
        override val languageName: String = "简中拼音"
    },
    ZHU_YIN {
        override val code: String = "Bopomofo"
        override val languageCode: String = "zh-TW-Bopomofo"
        override val languageName: String = "台湾注音"
    },
    UNREADABLE {
        override val code: String = "Mars"
        override val languageCode: String = "und"
        override val languageName: String = "火星文"
    },
    WIKIPEDIA_SIMPLIFIED {
        override val code: String = "WikiSimplified"
        override val languageCode: String = "zh-Hans"
        override val languageName: String = "维基简中"
    },
    WIKIPEDIA_TRADITIONAL {
        override val code: String = "WikiTraditional"
        override val languageCode: String = "zh-Hant"
        override val languageName: String = "维基繁中"
    }
    ;

    abstract val code: String
    abstract val languageCode: String
    abstract val languageName: String

    companion object : KSerializer<Converter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Converter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Converter = Converter(decoder.decodeString()) ?: UNKNOWN

        override fun serialize(encoder: Encoder, value: Converter): Unit = encoder.encodeString(value.code)
    }
}

@Suppress("FunctionName")
fun Converter(code: String, ignoreCase: Boolean = true): Converter? =
    Converter.values().firstOrNull {
        it.code.equals(code, ignoreCase) || it.name.equals(code, ignoreCase)
    }
