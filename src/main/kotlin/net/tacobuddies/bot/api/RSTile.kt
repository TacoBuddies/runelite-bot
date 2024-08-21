package net.tacobuddies.bot.api

import net.runelite.api.Perspective
import net.runelite.api.Tile
import net.runelite.api.coords.WorldPoint
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import java.awt.Shape

class RSTile(context: ScriptContext, val tile: Tile) : ContextProvider<ScriptContext>(context), Interactable {
    private val hitbox = Hitbox(context, this, false)

    val worldLocation: WorldPoint
        get() = tile.worldLocation

    override suspend fun interactWith(action: String, target: String?, identifier: Int?): Boolean {
        for(i in 0 ..< 3) {
            var point = context.calculations.tileToScreen(tile)
            if(point == null) {
                context.camera.lookAt(tile.worldLocation)
                point = context.calculations.tileToScreen(tile)
            }

            if(context.client.mouseCanvasPosition.distanceTo(point) > 1) {
                hoverMouse()
            }

            if(context.menu.clickMenuAction(action, target, identifier)) {
                return true
            }
        }

        return false
    }

    override suspend fun clickMouse(rightClick: Boolean): Boolean {
        return interactWith("Walk here", null, null)
    }

    override suspend fun hoverMouse(): Boolean {
        var target = context.calculations.tileToScreen(tile)
        if(target == null || !context.calculations.pointOnScreen(target)) {
            context.camera.lookAt(tile.worldLocation)
        }

        target = context.calculations.tileToScreen(tile) ?: return false
        for(i in 0 ..< 3) {
            if(context.client.mouseCanvasPosition.distanceTo(target) > 1) {
                context.mouse.moveTo(target)
            }

            if(context.client.mouseCanvasPosition.distanceTo(target) == 0) {
                return true
            }
        }

        return false
    }

    override fun getHitboxShape(): Shape? {
        return Perspective.getCanvasTilePoly(context.client, tile.localLocation)
    }

    override fun getHitbox(): Hitbox {
        return hitbox
    }

    fun distanceTo(other: RSTile): Int {
        return worldLocation.distanceTo(other.worldLocation)
    }
}