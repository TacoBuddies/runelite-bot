package net.tacobuddies.scripts.afk

import io.github.oshai.kotlinlogging.KotlinLogging
import net.runelite.api.GameState
import net.runelite.api.coords.WorldArea
import net.tacobuddies.bot.scriptable.Script
import net.tacobuddies.bot.scriptable.ScriptDescriptor

private val logger = KotlinLogging.logger {}

@ScriptDescriptor(
    name = "AFK Fishing",
    enabled = false,
    tags = ["afk", "fishing", "osps"]
)
class AFKFishing : Script() {
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

        if(!inventory.contains(*HARPOON_IDS) && !equipment.contains(*HARPOON_IDS)) {
            logger.error { "Must have a harpoon to start script" }
            return false
        }

        return true
    }

    override fun onStop() {}

    override suspend fun loop(): Int {
        if (client.localPlayer.animation == -1) {
            idleTicks++
            if (idleTicks >= 5) {
                val spiritPool = npcs.getClosestNPCById(NPC_SPIRIT_POOL)
                if (spiritPool == null) {
                    logger.error { "Unable to find spirit pool" }
                    return 5000
                }

                if (spiritPool.interactWith("Harpoon")) {
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
        private const val NPC_SPIRIT_POOL: Int = 16372
        val HARPOON_IDS = intArrayOf(
            311, 10129, 20128, 21031, 23762, 23823, 25059,
            23864, 25114, 25373
        )
    }
}