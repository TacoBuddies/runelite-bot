package net.tacobuddies.bot.scriptable

import kotlin.random.Random

open class ContextProvider<T>(val context: T) {
    fun random(min: Int, max: Int): Int {
        return min + if(max == min) { 0 } else { Random.nextInt(max - min) }
    }

    fun waitUntil(timeout: Int, condition: () -> Boolean) {
        var done: Boolean
        val startTime = System.currentTimeMillis()
        do {
            done = condition.invoke()
        } while(!done && System.currentTimeMillis() - startTime < timeout)
    }
}