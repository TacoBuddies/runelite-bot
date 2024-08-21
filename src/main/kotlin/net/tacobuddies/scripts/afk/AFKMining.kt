package net.tacobuddies.scripts.afk

import io.github.oshai.kotlinlogging.KotlinLogging
import net.runelite.api.GameState
import net.runelite.api.coords.WorldArea
import net.tacobuddies.bot.scriptable.Script
import net.tacobuddies.bot.scriptable.ScriptDescriptor

private val logger = KotlinLogging.logger {}

@ScriptDescriptor(
    name = "AFK Mining",
    enabled = false,
    tags = ["afk", "mining", "osps"]
)
class AFKMining : Script() {
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

        if(!inventory.contains(*PICKAXE_IDS) && !equipment.contains(*PICKAXE_IDS)) {
            logger.error { "Must have a pickaxe to start script" }
            return false
        }

        return true
    }

    override fun onStop() {}

    override suspend fun loop(): Int {
        if (client.localPlayer.animation == -1) {
            idleTicks++
            if (idleTicks >= 5) {
                val stone = objects.getNearestById(OBJ_RUNESTONE)
                if (stone == null) {
                    logger.error { "Unable to find runestone" }
                    return 5000
                }

                if (stone.interactWith("Mine")) {
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
        private const val OBJ_RUNESTONE: Int = 65492
        private val PICKAXE_IDS = intArrayOf(
            1265, 1267, 1269, 12297, 1273, 1271, 1275,
            11920, 12797, 23677, 25376, 20014
        )
    }
}