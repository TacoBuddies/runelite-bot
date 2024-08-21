package net.tacobuddies.scripts.afk

import io.github.oshai.kotlinlogging.KotlinLogging
import net.runelite.api.GameState
import net.runelite.api.coords.WorldArea
import net.tacobuddies.bot.scriptable.Script
import net.tacobuddies.bot.scriptable.ScriptDescriptor

private val logger = KotlinLogging.logger {}

@ScriptDescriptor(
    name = "AFK Woodcutting",
    enabled = false,
    tags = ["afk", "woodcutting", "osps"]
)
class AFKWoodcutting : Script() {
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

        if(!inventory.contains(*HATCHET_IDS) && !equipment.contains(*HATCHET_IDS)) {
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
                val tree = objects.getNearestById(OBJ_CRYSTAL_TREE)
                if (tree == null) {
                    logger.error { "Unable to find crystal tree" }
                    return 5000
                }

                if (tree.interactWith("Chop")) {
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
        private const val OBJ_CRYSTAL_TREE: Int = 65499
        private val HATCHET_IDS = intArrayOf(

        )
    }
}