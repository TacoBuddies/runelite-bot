package net.tacobuddies.scripts.debug

import net.tacobuddies.bot.scriptable.Script
import net.tacobuddies.bot.scriptable.ScriptDescriptor
import kotlin.random.Random

@ScriptDescriptor(
    name = "Mouse Movement Test",
    enabled = false
)
class TestMouseMovement : Script() {
    override fun onStart(): Boolean {
        return true
    }

    override fun onStop() {
    }

    override suspend fun loop(): Int {
        val x = Random.nextInt(0, client.canvasWidth)
        val y = Random.nextInt(0, client.canvasHeight)

        mouse.moveTo(x, y)

        return 5000
    }
}