package net.tacobuddies.scripts.afk

import io.github.oshai.kotlinlogging.KotlinLogging
import net.runelite.api.GameState
import net.runelite.api.coords.WorldArea
import net.tacobuddies.bot.scriptable.Script
import net.tacobuddies.bot.scriptable.ScriptDescriptor

private val logger = KotlinLogging.logger {}

@ScriptDescriptor(
    name = "AFK Runecrafting",
    enabled = false,
    tags = ["afk", "runecrafting", "osps"]
)
class AFKRunecrafting : Script() {
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

        if(!inventory.contains(ITEM_RUNESTONE_SHARD)) {
            logger.error { "Must have runestone shards to start script" }
            return false
        }

        return true
    }

    override fun onStop() {}

    override suspend fun loop(): Int {
        if (client.localPlayer.animation == -1) {
            idleTicks++
            if (idleTicks >= 5) {
                val altar = objects.getNearestById(OBJ_ALTAR)
                if (altar == null) {
                    logger.error { "Unable to find unstable altar" }
                    return 5000
                }

                if (altar.interactWith("Bind")) {
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
        private const val OBJ_ALTAR: Int = 65491
        private const val ITEM_RUNESTONE_SHARD = 32760
    }
}