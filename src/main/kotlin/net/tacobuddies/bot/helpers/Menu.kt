package net.tacobuddies.bot.helpers

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import net.runelite.api.MenuEntry
import net.runelite.api.Point
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import java.awt.Rectangle
import java.util.regex.Pattern
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}
class Menu(context: ScriptContext) : ContextProvider<ScriptContext>(context) {
    private val client = context.client
    private val mouse = context.mouse

    fun getMenuItems(): Array<MenuEntry> {
        return client.menuEntries.reversedArray()
    }

    fun hasEntry(predicate: (MenuEntry) -> Boolean): Boolean {
        return indexOfEntry(predicate) != -1
    }

    private fun indexOfEntry(predicate: (MenuEntry) -> Boolean): Int {
        try {
            val entries = getMenuItems()
            for (i in entries.indices) {
                if (predicate.invoke(entries[i])) {
                    return i
                }
            }
        } catch(ignored: NullPointerException) {}
        return -1
    }

    suspend fun clickMenuAction(action: String, target: String?, identifier: Int?): Boolean {
        var index = indexOfEntry { entry ->
            entry.option.contains(action, ignoreCase = true)
                    && if(target != null) { removeFormatting(entry.target).contains(target, ignoreCase = true) } else { true }
                    && if(identifier != null) { entry.identifier == identifier } else { true }
        }
        if(index == -1) {
            delay(100)
            index = indexOfEntry { entry ->
                entry.option.contains(action, ignoreCase = true)
                        && if(target != null) { removeFormatting(entry.target).contains(target, ignoreCase = true) } else { true }
                        && if(identifier != null) { entry.identifier == identifier } else { true }
            }
        }

        if(!client.isMenuOpen) {
            if(index == -1) {
                return false
            }

            if(index == 0) {
                context.mouse.clickAt(context.client.mouseCanvasPosition, false)
                return true
            }

            context.mouse.clickAt(context.client.mouseCanvasPosition, true)
            delay(random(50, 90).toDuration(DurationUnit.MILLISECONDS))
            index = indexOfEntry { entry ->
                entry.option.contains(action, ignoreCase = true)
                        && if(target != null) { removeFormatting(entry.target).contains(target, ignoreCase = true) } else { true }
                        && if(identifier != null) { entry.identifier == identifier } else { true }
            }
            return clickMenuIndex(index)
        } else if(index == -1) {
            while(client.isMenuOpen) {
                context.mouse.moveMouseRandomly(750, 750)
                delay(100)
            }
            return false
        }

        return clickMenuIndex(index)
    }

    private suspend fun clickMenuIndex(index: Int): Boolean {
        if(!client.isMenuOpen) {
            return false
        }

        val entries = getMenuItems()
        if(entries.size <= 1) {
            // We missed
            return false
        }

        val topLeft = Point(client.menuX, client.menuY)
        val bounds = Rectangle(topLeft.x, topLeft.y, client.menuWidth, client.menuHeight)

        val clickX = topLeft.x + random(4, client.menuWidth - 4)
        val clickY = topLeft.y + TOP_OF_MENU_BAR + ((MENU_ENTRY_HEIGHT * index) + random(2, MENU_ENTRY_HEIGHT - 2))

        if(bounds.contains(clickX, clickY)) {
            mouse.clickAt(clickX, clickY, false)
            return true
        } else {
            //TODO: If index is outside of bounds, scroll mouse wheel until it is visible
            logger.warn { "Menu item requires scrolling that is not yet implemented" }
            logger.warn { "Bounds[X=${bounds.x}, Y=${bounds.y}, Width=${bounds.width}, Height=${bounds.height}]"}
            logger.warn { "Click[X=${clickX}, Y=${clickY}]"}
            return false
        }
    }

    companion object {
        private const val TOP_OF_MENU_BAR: Int = 18
        private const val MENU_ENTRY_HEIGHT: Int = 15

        private val PATTERN: Pattern = Pattern.compile("<.+?>")

        fun removeFormatting(input: String?): String {
            return if(input == null) {
                "null"
            } else {
                PATTERN.matcher(input).replaceAll("")
            }
        }
    }
}