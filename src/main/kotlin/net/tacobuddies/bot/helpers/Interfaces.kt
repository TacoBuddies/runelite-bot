package net.tacobuddies.bot.helpers

import kotlinx.coroutines.delay
import net.runelite.api.widgets.Widget
import net.runelite.api.widgets.WidgetInfo
import net.runelite.client.eventbus.Subscribe
import net.tacobuddies.bot.api.RSWidget
import net.tacobuddies.bot.helpers.Menu.Companion.removeFormatting
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import java.awt.Rectangle

class Interfaces(context: ScriptContext) : ContextProvider<ScriptContext>(context) {

    @Subscribe
    fun get(index: Int) = RSWidget(context, context.client.getWidget(index, 0))

    @Subscribe
    fun getComponent(index: Int, child: Int) = RSWidget(context, context.client.getWidget(index, child))

    @Subscribe
    fun getComponent(info: WidgetInfo) = RSWidget(context, context.client.getWidget(info))

    fun canContinue(): Boolean {
        return getContinueComponent() != null
    }

    suspend fun clickContinue(): Boolean {
        val widget = getContinueComponent() ?: return false
        return widget.isValid && widget.clickMouse(false)
    }

    @Subscribe
    fun getContinueComponent(): RSWidget? {
        var widget: Widget? = context.client.getWidget(WidgetInfo.PACK(231, 5))
        if (widget != null && !widget.isHidden) {
            return RSWidget(context, widget)
        }

        if (widget == null) {
            widget = context.client.getWidget(WidgetInfo.PACK(217, 5))
            if (widget != null && !widget.isHidden) {
                return RSWidget(context, widget)
            }
        }

        if (widget == null) {
            widget = context.client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT)
            if (widget != null && !widget.isHidden) {
                return RSWidget(context, widget)
            }
        }

        if (widget == null) {
            widget = context.client.getWidget(WidgetInfo.PACK(233, 3))
            if (widget != null && !widget.isHidden) {
                return RSWidget(context, widget)
            }
        }

        if (widget == null) {
            widget = context.client.getWidget(WidgetInfo.PACK(11, 4))
            if (widget != null && !widget.isHidden) {
                return RSWidget(context, widget)
            }
        }

        if (widget == null) {
            widget = context.client.getWidget(WidgetInfo.PACK(229, 2))
            if (widget != null && !widget.isHidden) {
                return RSWidget(context, widget)
            }
        }

        return null
    }

    @Subscribe
    fun getDestroyItemComponent(): RSWidget? {
        val widget = context.client.getWidget(WidgetInfo.DESTROY_ITEM_YES)
        if (widget != null && !widget.isHidden) {
            return RSWidget(context, widget)
        }

        return null
    }

    suspend fun clickDialogueOption(widget: RSWidget, option: String): Boolean {
        if (widget.isValid) {
            for (child in widget.getComponents()) {
                if (child.text.contains(option, ignoreCase = true)) {
                    return child.clickMouse(false)
                }
            }
        }

        return false
    }

    suspend fun clickFirstDialogOption(widget: RSWidget, vararg options: String): Boolean {
        if (widget.isValid) {
            for (child in widget.getComponents()) {
                for (option in options) {
                    if (child.text.contains(option, ignoreCase = true)) {
                        return child.clickMouse(false)
                    }
                }
            }
        }

        return false
    }

    suspend fun clickComponent(widget: RSWidget, action: String): Boolean {
        if(!widget.isValid) {
            return false
        }

        val rect = widget.area
        if(rect.x == -1) {
            return false
        }

        val minX = rect.x + 2
        val minY = rect.y + 2
        val width = rect.width - 4
        val height = rect.height - 4

        val actual = Rectangle(minX, minY, width, height)
        val mousePos = context.client.mouseCanvasPosition
        if(actual.contains(mousePos.x, mousePos.y) && context.menu.hasEntry { it.option.contains(action, ignoreCase = true) }) {
            return context.menu.clickMenuAction(action, null, null)
        }

        context.mouse.moveTo(random(minX, minX + width), random(minY, minY + height))
        return context.menu.clickMenuAction(action, null, null)
    }

    @Subscribe
    suspend fun makeX(amount: Int, name: String): Boolean {
        val container = RSWidget(context, context.client.getWidget(270, 13))
        if(!container.isValid) {
            return false
        }

        for(i in 0 until 8) {
            val textWidget = container.getDynamicComponent(i) ?: return false
            if(textWidget.text.contains(name, ignoreCase = true)) {
                return makeX(amount, i)
            }
        }

        return false
    }

    @Subscribe
    suspend fun makeX(amount: Int, index: Int = 0): Boolean {
        setMakeXQuantity(amount)

        val buttonWidget = RSWidget(context, context.client.getWidget(270, 14 + index))
        return clickComponent(buttonWidget, "Make " + removeFormatting(buttonWidget.name))
    }

    private suspend fun setMakeXQuantity(amount: Int): Boolean {
        val childId = when(amount) {
            -1 -> 12
            1 -> 7
            5 -> 8
            else -> 11
        }

        val action = when(amount) {
            -1 -> "All"
            1 -> "1"
            5 -> "5"
            else -> "X"
        }

        val container = RSWidget(context, context.client.getWidget(270, childId))
        if(!container.isValid) {
            return false
        }

        val textWidget = container.getDynamicComponent(9)
        if(textWidget != null && !textWidget.text.contains("<col=ffffff>", ignoreCase = true)) {
            if(action != "X") {
                return clickComponent(textWidget, action)
            } else {
                delay(50)
                context.keyboard.sendKeys(amount.toString(), true)
                delay(50)

                if(!textWidget.text.contains("<col=ffffff>", ignoreCase = true)) {
                    return clickComponent(textWidget, amount.toString())
                }

                return true
            }
        }

        return true
    }
}