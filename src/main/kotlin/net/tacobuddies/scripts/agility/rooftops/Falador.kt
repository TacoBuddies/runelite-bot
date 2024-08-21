package net.tacobuddies.scripts.agility.rooftops

import net.runelite.api.GameState
import net.runelite.api.coords.WorldPoint
import net.tacobuddies.bot.scriptable.ScriptDescriptor
import net.tacobuddies.scripts.agility.AgilityObstacle

@ScriptDescriptor(
    name = "Rooftop - Falador",
    enabled = true,
    tags = ["agility", "rooftop"]
)
class Falador : RooftopScript(STEPS) {

    override fun onStart(): Boolean {
        return context.client.gameState == GameState.LOGGED_IN
    }

    override suspend fun onLapComplete() {
        //Do nothing - Course start can be seen from
        //end of course
    }

    companion object {
        val STEPS = arrayOf(
            AgilityObstacle(14898, "Climb", WorldPoint(3036, 3342, 3)),
            AgilityObstacle(14899, "Cross", WorldPoint(3047, 3344, 3)),
            AgilityObstacle(14901, "Cross", WorldPoint(3050, 3357, 3)),
            AgilityObstacle(14903, "Jump", WorldPoint(3048, 3361, 3)),
            AgilityObstacle(14904, "Jump", WorldPoint(3041, 3361, 3)),
            AgilityObstacle(14905, "Cross", WorldPoint(3028, 3354, 3)),
            AgilityObstacle(14911, "Cross", WorldPoint(3020, 3353, 3)),
            AgilityObstacle(14919, "Jump", WorldPoint(3018, 3349, 3)),
            AgilityObstacle(14920, "Jump", WorldPoint(3014, 3346, 3)),
            AgilityObstacle(14921, "Jump", WorldPoint(3013, 3342, 3)),
            AgilityObstacle(14922, "Jump", WorldPoint(3013, 3333, 3)),
            AgilityObstacle(14924, "Jump", WorldPoint(3019, 3333, 3)),
            AgilityObstacle(14925, "Jump", WorldPoint(3029, 3333, 0))
        )
    }
}