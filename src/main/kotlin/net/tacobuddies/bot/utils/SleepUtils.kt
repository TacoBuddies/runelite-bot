package net.tacobuddies.bot.utils

object SleepUtils {
    fun sleep(delay: Long) {
        try {
            Thread.sleep(delay)
        } catch(ignored: InterruptedException) {}
    }
}