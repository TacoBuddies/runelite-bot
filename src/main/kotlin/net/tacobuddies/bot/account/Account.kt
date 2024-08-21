package net.tacobuddies.bot.account

import kotlinx.serialization.Serializable
import net.tacobuddies.bot.utils.TOTP

@Serializable
data class Account(val username: String, val password: String, val secret: String? = null, val bankPin: String? = null) {
    fun generateOneTimePassword(): String {
        return if(secret != null) {
            TOTP.generateTOTP(secret)
        } else {
            ""
        }
    }
}
