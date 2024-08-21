package net.tacobuddies.scripts.debug

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import net.tacobuddies.bot.scriptable.Script
import net.tacobuddies.bot.scriptable.ScriptDescriptor

private val logger = KotlinLogging.logger {}

@ScriptDescriptor(
    name = "Inventory Test",
    enabled = false
)
class TestInventory : Script() {
    override fun onStart(): Boolean {
        return true
    }

    override fun onStop() {
    }

    override suspend fun loop(): Int {
        if(!inventory.isOpen) {
            inventory.open()
        }

        if(inventory.dropItem(995)) {
            delay(1500)
        }

        groundItems.getNearestById(995)?.interactWith("Take")

        return 5_000
    }
}