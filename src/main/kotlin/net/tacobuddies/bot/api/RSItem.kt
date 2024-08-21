package net.tacobuddies.bot.api

import net.runelite.api.Item
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import java.awt.Shape

class RSItem(context: ScriptContext, val id: Int, val quantity: Int, val component: RSWidget?, val item: RSWidgetItem?) : ContextProvider<ScriptContext>(context), Interactable {
    private val hitbox = Hitbox(context, this, true)
    private val definition = context.itemManager.getItem(id)

    constructor(context: ScriptContext, item: RSWidgetItem) : this(context, item.id, item.quantity, null, item)
    constructor(context: ScriptContext, item: RSWidget): this(context, item.itemId, item.stackSize, item, null)
    constructor(context: ScriptContext, widget: RSWidget, item: Item) : this(context, item.id, item.quantity, widget, null)

    val isComponentValid: Boolean
        get() = component?.isVisible() ?: false

    val isItemValid: Boolean
        get() = id != ID_INVALID && item?.isValid ?: false

    val name: String?
        get() = definition?.name ?: component?.name

    suspend fun interactWith(action: String): Boolean {
        return hitbox.interactWith(action, null, null)
    }

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
        return component?.getHitboxShape() ?: item?.getHitboxShape()
    }

    override fun getHitbox(): Hitbox {
        return hitbox
    }

    companion object {
        private const val ID_INVALID: Int = -1
    }
}