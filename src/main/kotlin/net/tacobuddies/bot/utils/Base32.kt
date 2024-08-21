package net.tacobuddies.bot.utils

import java.util.*

object Base32 {
    val BASE32_CHARS: CharArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray()
    val BITS_LOOKUP: IntArray = IntArray(128)

    init {
        for(i in BASE32_CHARS.indices) {
            BITS_LOOKUP[BASE32_CHARS[i].code] = i
        }
    }
}

fun String.toBase32(): String {
    val encoded = StringBuilder()
    var buffer = 0
    var bufferLen = 0

    for(b in this.toByteArray()) {
        buffer = buffer shl 8
        buffer = buffer or (b.toInt() and 0xFF)
        bufferLen += 8

        while(bufferLen >= 5) {
            val index = (buffer shr (bufferLen - 5)) and 0x1F
            encoded.append(Base32.BASE32_CHARS[index])
            bufferLen -= 5
        }
    }

    while(encoded.length % 8 != 0) {
        encoded.append('=')
    }

    return encoded.toString()
}

fun String.fromBase32(): ByteArray {
    var upper = this.uppercase().replace("[=]".toRegex(), "")
    val out = mutableListOf<Byte>()
    var buffer = 0
    var bufferLen = 0

    for(c in upper.toCharArray()) {
        buffer = buffer shl 5
        buffer = buffer or Base32.BITS_LOOKUP[c.code]
        bufferLen += 5

        while(bufferLen >= 8) {
            out.add((buffer shr (bufferLen - 8)).toByte())
            bufferLen -= 8
        }
    }

    return out.toByteArray()
}