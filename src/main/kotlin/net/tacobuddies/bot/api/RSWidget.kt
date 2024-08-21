package net.tacobuddies.bot.api

import net.runelite.api.Point
import net.runelite.api.widgets.Widget
import net.runelite.api.widgets.WidgetInfo
import net.runelite.client.eventbus.Subscribe
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import java.awt.Rectangle
import java.awt.Shape

class RSWidget(context: ScriptContext, private val widget: Widget?) : ContextProvider<ScriptContext>(context), Interactable {
    private val hitbox = Hitbox(context, this, true)

    val id: Int
        get() = widget?.id ?: -1

    private val parentWidget: Widget?
        get() = widget?.parent

    private val parentId: Int
        get() = parentWidget?.id ?: -1

    val absoluteX: Int
        get() = widget?.canvasLocation?.x ?: -1

    val absoluteY: Int
        get() = widget?.canvasLocation?.y ?: -1

    val area: Rectangle
        get() = widget?.bounds ?: Rectangle(-1, -1, -1, -1)

    val isValid: Boolean
        get() = widget != null

    @Subscribe
    fun isVisible(): Boolean {
        return isValid && (isSelfVisible() && !widget!!.isHidden)
    }

    @Subscribe
    fun isSelfVisible(): Boolean {
        return isValid && !widget!!.isSelfHidden
    }

    val hasListener: Boolean
        get() = widget?.hasListener() ?: false

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
        return widget?.bounds
    }

    override fun getHitbox(): Hitbox {
        return hitbox
    }

    @Subscribe
    fun getComponents(): List<RSWidget> {
        if(!isValid) {
            return emptyList()
        }

        val components = mutableListOf<RSWidget>()
        widget!!.dynamicChildren?.also { children ->
            val childComponents = toRSWidget(children)
            for(component in childComponents) {
                components.addAll(component.getComponents())
            }
        }

        widget.staticChildren?.also { children ->
            val childComponents = toRSWidget(children)
            for(component in childComponents) {
                components.addAll(component.getComponents())
            }
        }

        widget.nestedChildren?.also { children ->
            val childComponents = toRSWidget(children)
            for(component in childComponents) {
                components.addAll(component.getComponents())
            }
        }

        if(components.isEmpty()) {
            components.add(this)
        }

        return components.toList()
    }

    private fun toRSWidget(widgets: Array<Widget>): List<RSWidget> {
        val components = mutableListOf<RSWidget>()
        for(i in widgets.indices) {
            components.add(i, RSWidget(context, widgets[i]))
        }
        return components.toList()
    }

    fun getDynamicComponent(index: Int): RSWidget? {
        if(widget == null) return null
        return RSWidget(context, widget.getChild(index))
    }

    fun getDynamicTextComponent(text: String): RSWidget? {
        if(widget == null)
            return null

        for(w in widget.dynamicChildren) {
            if(w.text.equals(text, ignoreCase = true)) {
                return RSWidget(context, w)
            }
        }

        return null
    }

    @Subscribe
    fun getComponent(index: Int): RSWidget {
        return RSWidget(context, context.client.getWidget(WidgetInfo.TO_GROUP(id), index))
    }

    val borderThickness: Int
        get() = widget?.borderType ?: -1

    val index: Int
        get() = if(widget != null) { id - parentId } else { -1 }

    val stackSize: Int
        get() = widget?.itemQuantity ?: -1

    val name: String
        get() = widget?.name?.replace("<.*?>".toRegex(), "") ?: ""

    val height: Int
        get() {
            if(!isInScrollableArea) {
                return realHeight
            }

            return widget?.height?.minus(4) ?: -1
        }

    @Subscribe
    fun getSpriteId(): Int {
        return widget?.spriteId ?: -1
    }

    val itemId: Int
        get() = widget?.itemId ?: -1

    val modelId: Int
        get() = widget?.modelId ?: -1

    fun getParent(): RSWidget {
        return RSWidget(context, parentWidget)
    }

    val groupIndex: Int
        get() = widget?.id?.let { WidgetInfo.TO_GROUP(it) } ?: -1

    val childIndex: Int
        get() = widget?.id?.let { WidgetInfo.TO_CHILD(it) } ?: -1

    val location: Point
        get() = Point(absoluteX, absoluteY)

    val center: Point
        get() = Point(absoluteX + width / 2, absoluteY + height / 2)

    val relativeX: Int
        get() = widget?.relativeX ?: -1

    val relativeY: Int
        get() = widget?.relativeY ?: -1

    val verticalScrollPosition: Int
        get() = widget?.scrollY ?: -1

    val horizontalScrollPosition: Int
        get() = widget?.scrollX ?: -1

    val scrollableContentHeight: Int
        get() = widget?.scrollHeight ?: -1

    val scrollableContentWidth: Int
        get() = widget?.scrollWidth ?: -1

    val realHeight: Int
        get() = widget?.scrollHeight ?: -1

    val realWidth: Int
        get() = widget?.scrollWidth ?: -1

    val isInScrollableArea: Boolean
        get() {
            if(parentId == -1) {
                return false
            }

            var scrollableArea = this
            while((scrollableArea.scrollableContentHeight == 0) && (scrollableArea.parentId != -1)) {
                scrollableArea = scrollableArea.getParent()
            }

            return scrollableArea.scrollableContentHeight != 0
        }

    val isVisibleInScrollableArea: Boolean
        get() {
            if(parentId == -1 || !isInScrollableArea) {
                return false
            }

            var scrollableArea = this
            while((scrollableArea.scrollableContentHeight == 0) && (scrollableArea.parentId != -1)) {
                scrollableArea = scrollableArea.getParent()
            }

            return relativeX + height / 2 > scrollableArea.verticalScrollPosition &&
                    relativeY - height / 2 < scrollableArea.verticalScrollPosition + scrollableArea.height
        }

    val selectedActionName: String
        get() = widget?.targetVerb ?: ""

    val text: String
        get() = widget?.text ?: ""

    val backgroundColor: Int
        get() = widget?.textColor ?: -1

    val fontId: Int
        get() = widget?.fontId ?: -1

    val isTextShadowed: Boolean
        get() = widget?.textShadowed ?: false

    val type: Int
        get() = widget?.type ?: -1

    val actions: List<String>
        get() = widget?.actions?.toList() ?: emptyList()

    val width: Int
        get() {
            if(!isInScrollableArea) {
                return realWidth
            }

            return (widget?.width?.minus(4)) ?: -1
        }

    fun containsAction(phrase: String): Boolean {
        if(actions.isEmpty()) {
            return false
        }

        for(action in actions) {
            if(action.contains(phrase, ignoreCase = true)) {
                return true
            }
        }

        return false
    }

    fun containsText(phrase: String): Boolean = text.contains(phrase, ignoreCase = true)
}