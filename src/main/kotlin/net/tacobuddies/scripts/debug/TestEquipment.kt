package net.tacobuddies.scripts.debug

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import net.tacobuddies.bot.helpers.Equipment
import net.tacobuddies.bot.scriptable.Script
import net.tacobuddies.bot.scriptable.ScriptDescriptor

private val logger = KotlinLogging.logger {}

@ScriptDescriptor(
    name = "Equipment Test",
    enabled = false
)
class TestEquipment : Script() {
    override fun onStart(): Boolean {
        return true
    }

    override fun onStop() {
    }

    override suspend fun loop(): Int {
        if(!equipment.isOpen) {
            equipment.open()
        }

        for(slot in Equipment.Slot.entries) {
            logger.info { "$slot = ${equipment[slot]?.id ?: -1}"}
        }

        return 5_000
    }
}