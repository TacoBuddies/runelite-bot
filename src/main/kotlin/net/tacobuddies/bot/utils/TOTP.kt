package net.tacobuddies.bot.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

private val logger = KotlinLogging.logger {}

object TOTP {
    const val HMAC_ALGO: String = "HmacSHA1"
    const val TOTP_LENGTH: Int = 6
    const val TIME_STEP: Int = 30

    fun generateTOTP(secret: String): String {
        val timeInterval = System.currentTimeMillis() / 1000 / TIME_STEP
        return generateTOTP(secret, timeInterval)
    }

    fun generateTOTP(secretKey: String, timeInterval: Long): String {
        var time = timeInterval
        try {
            val decodedKey = secretKey.fromBase32()
            val timeIntervalBytes = ByteArray(8).apply {
                for (i in 7 downTo 0) {
                    this[i] = (time and 0xFFL).toByte()
                    time = time shr 8
                }
            }

            val hmac = Mac.getInstance(HMAC_ALGO).apply {
                init(SecretKeySpec(decodedKey, HMAC_ALGO))
            }

            val hash = hmac.doFinal(timeIntervalBytes)
            val offset = hash[hash.size - 1].toInt() and 0xF

            val mostSignificantByte = ((hash[offset].toInt() and 0x7F) shl 24).toLong()
            val secondMostSignificantByte = ((hash[offset + 1].toInt() and 0xFF) shl 16).toLong()
            val thirdMostSignificantByte = ((hash[offset + 2].toInt() and 0xFF) shl 8).toLong()
            val leastSignificantByte = (hash[offset + 3].toInt() and 0xFF).toLong()

            val binaryCode = (mostSignificantByte
                    or secondMostSignificantByte
                    or thirdMostSignificantByte
                    or leastSignificantByte)

            val totp: Int = (binaryCode % 10.0.pow(TOTP_LENGTH.toDouble())).toInt()
            return String.format("%0" + TOTP_LENGTH + "d", totp)
        } catch (e: Exception) {
            logger.error(e) { "Failed to generate TOTP" }
        }
        return ""
    }
}