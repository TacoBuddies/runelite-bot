package net.tacobuddies.scripts.afk

import io.github.oshai.kotlinlogging.KotlinLogging
import net.runelite.api.GameState
import net.runelite.api.coords.WorldArea
import net.tacobuddies.bot.scriptable.Script
import net.tacobuddies.bot.scriptable.ScriptDescriptor

private val logger = KotlinLogging.logger {}

@ScriptDescriptor(
    name = "AFK Farming",
    enabled = false,
    tags = ["afk", "farming", "osps"]
)
class AFKFarming : Script() {
    private var idleTicks: Int = 0

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

        if(!inventory.contains(*SECATEUR_IDS) && !equipment.contains(*SECATEUR_IDS)) {
            logger.error { "Must have secateurs to start script" }
            return false
        }

        return true
    }

    override fun onStop() {}

    override suspend fun loop(): Int {
        if (client.localPlayer.animation == -1) {
            idleTicks++
            if (idleTicks >= 5) {
                val yggdrasil = npcs.getClosestNPCById(NPC_YGGDRASIL)
                if (yggdrasil == null) {
                    logger.error { "Unable to find yggdrasil" }
                    return 5000
                }

                if (yggdrasil.interactWith("Harvest")) {
                    animationStart()
                    idleTicks = 0
                }
            }
        } else {
            idleTicks = 0
        }
        return 1000
    }

    companion object {
        private const val NPC_YGGDRASIL: Int = 16374
        val SECATEUR_IDS = intArrayOf(
            5329, 7409
        )
    }
}