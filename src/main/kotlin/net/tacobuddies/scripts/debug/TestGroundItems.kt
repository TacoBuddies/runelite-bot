package net.tacobuddies.scripts.debug

import io.github.oshai.kotlinlogging.KotlinLogging
import net.tacobuddies.bot.scriptable.Script
import net.tacobuddies.bot.scriptable.ScriptDescriptor

private val logger = KotlinLogging.logger {}

@ScriptDescriptor(
    name = "Ground Item Test",
    enabled = false
)
class TestGroundItems : Script() {
    override fun onStart(): Boolean {
        return true
    }

    override fun onStop() {
    }

    override suspend fun loop(): Int {
        val items = groundItems.getAll()
        for(item in items) {
            logger.info { "Item found: ${item.item.id}x${item.item.quantity}" }
        }

        return 5000
    }
}