package net.tacobuddies.scripts.agility.rooftops

import net.runelite.api.GameState
import net.runelite.api.coords.WorldPoint
import net.tacobuddies.bot.scriptable.ScriptDescriptor
import net.tacobuddies.scripts.agility.AgilityObstacle

@ScriptDescriptor(
    name = "Rooftop - Ardougne",
    enabled = true,
    tags = ["agility", "rooftop"]
)
class Ardougne : RooftopScript(STEPS) {

    override fun onStart(): Boolean {
        return context.client.gameState == GameState.LOGGED_IN
    }

    override suspend fun onLapComplete() {
        //Do nothing. Course ends close enough to start
        //to resume without walking or teleporting
    }

    companion object {
        val STEPS = arrayOf(
            AgilityObstacle(15608, "Climb-up", WorldPoint(2671, 3299, 3)),
            AgilityObstacle(15609, "Jump", WorldPoint(2665, 3318, 3)),
            AgilityObstacle(26635, "Walk-on", WorldPoint(2656, 3318, 3)),
            AgilityObstacle(15610, "Jump", WorldPoint(2653, 3314, 3)),
            AgilityObstacle(15611, "Jump", WorldPoint(2651, 3309, 3)),
            AgilityObstacle(28912, "Balance-across", WorldPoint(2656, 3297, 3)),
            AgilityObstacle(15612, "Jump", WorldPoint(2668, 3297, 0)),
        )
    }
}