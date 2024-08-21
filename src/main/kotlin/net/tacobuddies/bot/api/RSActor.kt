package net.tacobuddies.bot.api

import net.runelite.api.Actor
import net.runelite.api.coords.WorldPoint
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import java.awt.Shape

abstract class RSActor(context: ScriptContext, protected val actor: Actor) : ContextProvider<ScriptContext>(context), Interactable {
    private val hitbox = Hitbox(context, this, false)

    val worldLocation: WorldPoint
        get() = actor.worldLocation

    val location: RSTile?
        get() {
            val tile = context.calculations.getTile(worldLocation) ?: return null
            return RSTile(context, tile)
        }

    val interacting: Actor?
        get() = actor.interacting

    fun distanceTo(other: RSObject): Int {
        return distanceTo(other.worldLocation)
    }

    fun distanceTo(other: RSActor): Int {
        return distanceTo(other.worldLocation)
    }

    fun distanceTo(tile: RSTile?): Int {
        if(tile == null) return Int.MAX_VALUE
        return worldLocation.distanceTo(tile.worldLocation)
    }

    fun distanceTo(point: WorldPoint): Int {
        return worldLocation.distanceTo(point)
    }

    override suspend fun interactWith(action: String, target: String?, identifier: Int?): Boolean {
        if(hitbox.interactWith(action, target, identifier)) {
            return true
        } else {
            context.camera.lookAt(actor.worldLocation)
            return hitbox.interactWith(action, target, identifier)
        }
    }

    override suspend fun clickMouse(rightClick: Boolean): Boolean {
        if(hitbox.clickMouse(rightClick)) {
            return true
        } else {
            context.camera.lookAt(actor.worldLocation)
            return hitbox.clickMouse(rightClick)
        }
    }

    override suspend fun hoverMouse(): Boolean {
        if(hitbox.hoverMouse()) {
            return true
        } else {
            context.camera.lookAt(actor.worldLocation)
            return hitbox.hoverMouse()
        }
    }

    override fun getHitboxShape(): Shape? {
        return actor.convexHull
    }

    override fun getHitbox(): Hitbox {
        return hitbox
    }
}