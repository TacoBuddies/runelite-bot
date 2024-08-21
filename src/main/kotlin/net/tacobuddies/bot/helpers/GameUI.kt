package net.tacobuddies.bot.helpers

import net.runelite.api.widgets.Widget
import net.runelite.api.widgets.WidgetInfo
import net.tacobuddies.bot.api.constants.InterfaceTab
import net.tacobuddies.bot.api.constants.Viewports
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext

class GameUI(context: ScriptContext) : ContextProvider<ScriptContext>(context) {

    fun getTab(tab: InterfaceTab): Widget? {
        val layout = getViewportLayout() ?: return null
        val widgetInfo = when(layout) {
            Viewports.Fixed -> tab.fixed
            Viewports.ResizableClassic -> tab.resizableClassic
            Viewports.ResizableModern -> tab.resizableModern
        }

        return context.client.getWidget(widgetInfo)
    }

    fun getViewportLayout(): Viewports? {
        val fixedMinimap = context.client.getWidget(WidgetInfo.FIXED_VIEWPORT_MINIMAP)
        val resizableClassicMinimap = context.client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_WIDGET)
        val resizableModernMinimap = context.client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_WIDGET)

        if(fixedMinimap != null && !fixedMinimap.isHidden) {
            return Viewports.Fixed
        } else if(resizableClassicMinimap != null && !resizableClassicMinimap.isHidden) {
            return Viewports.ResizableClassic
        } else if(resizableModernMinimap != null && !resizableModernMinimap.isHidden) {
            return Viewports.ResizableModern
        }

        return null
    }

    companion object {
        enum class ViewportMode {
            FIXED, RESIZABLE_CLASSIC, RESIZABLE_MODERN, UNKNOWN
        }
    }
}