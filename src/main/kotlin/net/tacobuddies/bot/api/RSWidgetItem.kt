package net.tacobuddies.bot.api

import net.runelite.api.Point
import net.runelite.api.widgets.WidgetItem
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import java.awt.Rectangle
import java.awt.Shape

class RSWidgetItem(context: ScriptContext, private val item: WidgetItem?) : ContextProvider<ScriptContext>(context), Interactable {
    private val hitbox = Hitbox(context, this, true)
    private val parent = item?.widget

    val isValid: Boolean
        get() = item != null

    val area: Rectangle?
        get() = item?.canvasBounds

    val id: Int
        get() = item?.id ?: -1

    val quantity: Int
        get() = item?.quantity ?: -1

    val location: Point?
        get() = item?.canvasLocation

    override suspend fun interactWith(action: String, target: String?, identifier: Int?): Boolean {
        return hitbox.interactWith(action, target, identifier)
    }

    override suspend fun clickMouse(rightClick: Boolean): Boolean {
        return hitbox.clickMouse(rightClick)
    }

    override suspend fun hoverMouse(): Boolean {
        return hitbox.hoverMouse()
    }

    override fun getHitboxShape(): Shape? {
        return item?.canvasBounds
    }

    override fun getHitbox(): Hitbox {
        return hitbox
    }
}