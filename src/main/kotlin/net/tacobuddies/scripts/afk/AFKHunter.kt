package net.tacobuddies.scripts.afk

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import net.runelite.api.GameState
import net.runelite.api.coords.WorldArea
import net.tacobuddies.bot.scriptable.Script
import net.tacobuddies.bot.scriptable.ScriptDescriptor

private val logger = KotlinLogging.logger {}

@ScriptDescriptor(
    name = "AFK Hunter",
    enabled = false,
    tags = ["afk", "hunter", "osps"]
)
class AFKHunter : Script() {
    override fun onStart(): Boolean {
        if(client.gameState != GameState.LOGGED_IN) {
            logger.error { "Must be logged in to start script" }
            return false
        }

        val afkZone = WorldArea(1977, 3297, 18, 23, 0)
        if (!client.localPlayer.worldLocation.isInArea(afkZone)) {
            logger.error { "Must be in AFK zone to start script" }
            return false
        }

        if(!inventory.contains(ITEM_NOOSE_WAND) && !equipment.contains(ITEM_NOOSE_WAND)) {
            logger.error { "Must have a noose wand to start script" }
            return false
        }

        return true
    }

    override fun onStop() {}

    override suspend fun loop(): Int {
        if (client.localPlayer.interacting == null) {
            val herbiboar = npcs.getClosestNPCById(NPC_HERBIBOAR)
            if (herbiboar == null) {
                logger.error { "Unable to find herbiboar" }
                return 5000
            }

            if(herbiboar.interactWith("Investigate")) {
                delay(5000)
            }
        }
        return 1000
    }

    companion object {
        private const val NPC_HERBIBOAR: Int = 16361
        private const val ITEM_NOOSE_WAND = 10150
    }
}