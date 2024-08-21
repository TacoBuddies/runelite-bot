package net.tacobuddies.bot.account

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import java.io.File

private val logger = KotlinLogging.logger {}
class AccountManager {
    private val accounts = mutableListOf<Account>()
    fun getAccounts(): List<Account> = accounts.toList()

    fun load(file: File) {
        if(!file.exists())
            return

        val loaded = Json.decodeFromString<List<Account>>(file.readText())
        accounts.addAll(loaded)

        logger.info { "Loaded ${loaded.size} accounts" }
    }

    fun getDefaultAccount(): Account? {
        return System.getProperty("net.tacobuddies.login")?.let { getAccount(it) }
    }

    fun getAccount(username: String): Account? {
        return accounts.find { it.username.equals(username, ignoreCase = true) }
    }
}