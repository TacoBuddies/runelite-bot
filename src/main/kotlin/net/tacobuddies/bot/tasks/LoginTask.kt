package net.tacobuddies.bot.tasks

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import net.runelite.api.GameState
import net.tacobuddies.bot.account.Account
import net.tacobuddies.bot.scriptable.TaskContext

private val logger = KotlinLogging.logger {}
class LoginTask(private val account: Account) : Task {
    override suspend fun run(context: TaskContext) {
        val client = context.client
        val keyboard = context.keyboard

        while(client.gameState != GameState.LOGGED_IN) {
            when(client.loginIndex) {
                0 -> {
                    keyboard.sendKeys("\n", false)
                    delay(1000)
                }
                2 -> {
                    client.username = account.username
                    client.setPassword(account.password)
                    if(context.client.currentLoginField == 0) {
                        keyboard.sendKeys("\n", false)
                        delay(1000)
                    }

                    if(context.client.currentLoginField == 1) {
                        keyboard.sendKeys("\n", false)
                        delay(1000)
                    }
                }
                4 -> {
                    val otp = account.generateOneTimePassword()
                    if(otp == "") {
                        logger.error { "${account.username} has 2FA setup but did not provide the 2FA key required for auto login" }
                        return
                    }

                    client.setOtp(otp)
                    keyboard.sendKeys("\n", false)
                    delay(1000)
                }
            }
        }
    }
}