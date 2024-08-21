package net.tacobuddies.scripts.agility.rooftops

import kotlinx.coroutines.delay
import net.runelite.api.GameState
import net.runelite.api.coords.WorldPoint
import net.runelite.api.widgets.WidgetInfo
import net.tacobuddies.bot.scriptable.ScriptDescriptor
import net.tacobuddies.scripts.agility.AgilityObstacle

@ScriptDescriptor(
    name = "Rooftop - Rellekka",
    enabled = true,
    tags = ["agility", "rooftop"]
)
class Rellekka : RooftopScript(STEPS) {

    override fun onStart(): Boolean {
        return context.client.gameState == GameState.LOGGED_IN
    }

    override suspend fun onLapComplete() {
        val widget = context.interfaces.getComponent(WidgetInfo.CHATBOX_REPORT_TEXT)
        if(widget.isValid) {
            widget.interactWith("Last-teleport", null, null)
            await(atLocation(WorldPoint(2626, 3678, 0), 3000))

            while(context.players.localPlayer.worldLocation.distanceTo(WorldPoint(2625, 3678, 0)) > 10) {
                widget.clickMouse(false)
                delay(600)
                context.keyboard.sendKeys("5168", false)
                await(atLocation(WorldPoint(2626, 3678, 0), 3000))
            }
        }
    }

    companion object {
        val STEPS = arrayOf(
            AgilityObstacle(14946, "Climb", WorldPoint(2626, 3676, 3)),
            AgilityObstacle(14947, "Leap", WorldPoint(2622, 3668, 3)),
            AgilityObstacle(14987, "Cross", WorldPoint(2627, 3654, 3)),
            AgilityObstacle(14990, "Leap", WorldPoint(2639, 3653, 3)),
            AgilityObstacle(14991, "Hurdle", WorldPoint(2643, 3657, 3)),
            AgilityObstacle(14992, "Cross", WorldPoint(2655, 3670, 3)),
            AgilityObstacle(14994, "Jump-in", WorldPoint(2652, 3676, 0)),
        )
    }
}