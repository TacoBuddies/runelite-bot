package net.tacobuddies.bot.scriptable

import net.runelite.api.Client
import net.tacobuddies.bot.helpers.Keyboard
import net.tacobuddies.bot.helpers.Mouse

open class TaskContext(val client: Client) {
    val mouse = Mouse(this)
    val keyboard = Keyboard(this)
}