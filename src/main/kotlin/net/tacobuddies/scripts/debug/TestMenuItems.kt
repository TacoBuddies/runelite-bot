package net.tacobuddies.scripts.debug

import io.github.oshai.kotlinlogging.KotlinLogging
import net.tacobuddies.bot.scriptable.Script
import net.tacobuddies.bot.scriptable.ScriptDescriptor

private val logger = KotlinLogging.logger {}

@ScriptDescriptor(
    name = "Menu Test",
    enabled = false
)
class TestMenuItems : Script() {
    override fun onStart(): Boolean {
        return true
    }

    override fun onStop() {
    }

    override suspend fun loop(): Int {
        if(client.isMenuOpen) {
            menu.getMenuItems().forEachIndexed { index, entry ->
                logger.info { "Entry $index = $entry"}
            }
        }
        return 1000
    }
}