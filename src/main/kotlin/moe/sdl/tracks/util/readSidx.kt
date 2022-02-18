package moe.sdl.tracks.util

import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readUInt
import io.ktor.utils.io.core.readULong
import io.ktor.utils.io.core.readUShort
import io.ktor.utils.io.core.writeFully

/**
 * Parses a sidx defined in ISO 14496-12
 *
 * @param bytes the source data
 * @param offset the offset of source data to first sidx byte, including header
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun readSidx(bytes: ByteArray, offset: Int = 0): Sidx {
    val pkt = buildPacket {
        writeFully(bytes)
    }
    pkt.discard(offset)
    pkt.discard(8) // header size

    val fullAtom = pkt.readUInt()
    val version = 0x000000FFu and (fullAtom shr 24)

    pkt.discard(4) // reference_ID
    val timescale = pkt.readUInt()

    val earliestPresentationTime: ULong
    val firstOffset: ULong
    if (version == 0u) {
        earliestPresentationTime = pkt.readUInt().toULong()
        firstOffset = pkt.readUInt().toULong()
    } else {
        earliestPresentationTime = pkt.readULong()
        firstOffset = pkt.readULong()
    }

    val reserved = pkt.readUShort()
    val referenceCount = pkt.readUShort()

    val entries = arrayListOf<SidxEntry>()
    for (i in 1..referenceCount.toInt()) {
        val fst4bits = pkt.readUInt()
        val type = (0x80_00_00_00u and fst4bits) shr 31
        val size = (0x7F_FF_FF_FFu and fst4bits)
        val duration = pkt.readUInt()
        val snd4bits = pkt.readUInt()
        val startsWithSap = ((0x80_00_00_00u and snd4bits) shr 31) == 0x1u
        val sapType = (0x70_00_00_00u and snd4bits) shr 28
        val sapDeltaTime = (0x0F_FF_FF_FFu and snd4bits)
        SidxEntry(
            type, size, duration, startsWithSap, sapType, sapDeltaTime
        ).also(entries::add)
    }
    return Sidx(version, timescale, earliestPresentationTime, firstOffset, reserved, referenceCount, entries)
}

data class Sidx(
    val version: UInt,
    val timescale: UInt,
    val earliestPresentationTime: ULong,
    val firstOffset: ULong,
    val reserved: UShort,
    val referenceCount: UShort,
    val entries: ArrayList<SidxEntry>,
)

data class SidxEntry(
    val referenceType: UInt,
    val referencedSize: UInt,
    val subsegmentDuration: UInt,
    val startsWithSap: Boolean,
    val sapType: UInt,
    val sapDeltaTime: UInt,
)
