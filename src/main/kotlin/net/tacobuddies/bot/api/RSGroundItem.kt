package net.tacobuddies.bot.api

import net.runelite.api.TileItem
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import java.awt.Shape

class RSGroundItem(context: ScriptContext, val tile: RSTile, val item: TileItem) : ContextProvider<ScriptContext>(context), Interactable {
    private val hitbox = Hitbox(context, this, false)

    suspend fun interactWith(action: String): Boolean {
        return interactWith(action, null, item.id)
    }

    override suspend fun interactWith(action: String, target: String?, identifier: Int?): Boolean {
        if(hitbox.interactWith(action, target, identifier)) {
            return true
        } else {
            context.camera.lookAt(tile.worldLocation)
            return hitbox.interactWith(action, target, identifier)
        }
    }

    override suspend fun clickMouse(rightClick: Boolean): Boolean {
        if(hitbox.clickMouse(rightClick)) {
            return true
        } else {
            context.camera.lookAt(tile.worldLocation)
            return hitbox.clickMouse(rightClick)
        }
    }

    override suspend fun hoverMouse(): Boolean {
        if(hitbox.hoverMouse()) {
            return true
        } else {
            context.camera.lookAt(tile.worldLocation)
            return hitbox.hoverMouse()
        }
    }

    override fun getHitboxShape(): Shape? {
        return tile.getHitboxShape()
    }

    override fun getHitbox(): Hitbox {
        return hitbox
    }
}