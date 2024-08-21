package net.tacobuddies.bot.helpers

import kotlinx.coroutines.delay
import net.runelite.api.InventoryID
import net.runelite.api.widgets.WidgetInfo
import net.tacobuddies.bot.api.RSItem
import net.tacobuddies.bot.api.RSNPC
import net.tacobuddies.bot.api.RSObject
import net.tacobuddies.bot.api.RSWidget
import net.tacobuddies.bot.api.constants.InterfaceTab
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import kotlin.math.min

class Inventory(context: ScriptContext) : ContextProvider<ScriptContext>(context) {
    val `interface`: RSWidget?
        get() {
            val widgets = mapOf(
                KEY_BANK to context.interfaces.getComponent(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER),
                KEY_STORE to context.interfaces.getComponent(WidgetInfo.SHOP_INVENTORY_ITEMS_CONTAINER),
                KEY_GE to context.interfaces.getComponent(WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER),
                KEY_INVENTORY to context.interfaces.getComponent(WidgetInfo.INVENTORY)
            )

            for(entry in widgets.entries) {
                if(isOpen(entry.value) || entry.key == KEY_INVENTORY) {
                    return entry.value
                }
            }
            return null
        }

    val isOpen: Boolean
        get() = context.game.currentTab == InterfaceTab.Inventory

    val selectedItemIndex: Int
        get() {
            if(!isOpen) {
                return -1
            }

            val invInterface = `interface` ?: return -1
            val components = invInterface.getComponents()
            for(i in 0 until min(28, components.size)) {
                if(components[i].borderThickness == 2) {
                    return i
                }
            }

            return -1
        }

    val selectedItem: RSItem?
        get() = this[selectedItemIndex]

    val items: List<RSItem?>
        get() {
        val container = context.client.getItemContainer(InventoryID.INVENTORY) ?: return emptyList()
        val items = container.items

        val invInterface = `interface` ?: return emptyList()
        val invItems = invInterface.getComponents()

        val out = MutableList<RSItem?>(container.size()) { null }
        for(i in items.indices) {
            if(items[i].id != -1) {
                out[i] = RSItem(context, invItems[i], items[i])
            }
        }

        return out.toList()
    }

    val itemGrid: Array<IntArray>
        get() {
            val inventory = Array(7) { IntArray(4) }
            for (i in items.indices) {
                val column = i % 4
                val row = i / 4

                if (items[i] != null) {
                    inventory[row][column] = items[i]!!.id
                } else {
                    inventory[row][column] = -1
                }
            }

            return inventory
        }

    operator fun get(index: Int): RSItem? {
        val container = context.client.getItemContainer(InventoryID.INVENTORY) ?: return null
        if(index < 0 || index >= container.size()) {
            return null
        }

        val item = container.getItem(index) ?: return null
        if(item.id == -1) {
            return null
        }

        val invInterface = `interface` ?: return null
        val comp = invInterface.getDynamicComponent(index) ?: return null

        return RSItem(context, comp, item)
    }

    fun getWithIds(vararg ids: Int): List<RSItem> {
        val out = mutableListOf<RSItem>()
        for(item in items.filterNotNull()) {
            for(id in ids) {
                if(item.id == id) {
                    out.add(item)
                    break
                }
            }
        }

        return out.toList()
    }

    fun getWithNames(vararg names: String): List<RSItem> {
        val out = mutableListOf<RSItem>()
        for(item in items.filterNotNull()) {
            for(name in names) {
                if(item.name?.equals(name) == true) {
                    out.add(item)
                    break
                }
            }
        }

        return out.toList()
    }

    fun first(vararg ids: Int): RSItem? {
        return items
            .filterNotNull()
            .firstOrNull { it.id in ids }
    }

    fun contains(vararg ids: Int): Boolean {
        return items
            .filterNotNull()
            .any { it.id in ids }
    }

    fun countExcludeById(includeStacks: Boolean, vararg ids: Int): Int {
        return items
            .filterNotNull()
            .filter { it.id != -1 && it.id != EMPTY_SLOT_ITEM_ID }
            .filter { it.id !in ids }
            .sumOf { if(includeStacks) { it.quantity } else { 1 } }
    }

    fun countById(includeStacks: Boolean, vararg ids: Int): Int {
        return items
            .filterNotNull()
            .filter { it.id != -1 && it.id != EMPTY_SLOT_ITEM_ID }
            .filter { it.id in ids }
            .sumOf { if(includeStacks) { it.quantity } else { 1 } }
    }

    fun count(includeStacks: Boolean): Int {
        return items
            .filterNotNull()
            .filter { it.id != -1 && it.id != EMPTY_SLOT_ITEM_ID }
            .sumOf { if(includeStacks) { it.quantity} else { 1 } }
    }

    suspend fun open(): Boolean {
        return context.game.openTab(InterfaceTab.Inventory)
    }

    suspend fun dropIndex(index: Int) = dropItem(this[index])
    suspend fun dropItem(itemId: Int) = dropItem(first(itemId))
    suspend fun dropItem(item: RSItem?): Boolean {
        return item?.interactWith("Drop") ?: return false
    }

    suspend fun moveItem(index: Int, target: Int) = moveItem(this[index], target)
    suspend fun moveItem(item: RSItem?, target: Int): Boolean {
        if(item != null) {
            if(item.hoverMouse()) {
                val id = item.id
                val invInterface = `interface` ?: return false
                val comp = invInterface.getDynamicComponent(target) ?: return false
                context.mouse.dragTo(comp.center)
                waitUntil(1000) {
                    this[target] != null && this[target]!!.id == id
                }
                return true
            }
        }
        return false
    }

    suspend fun selectIndex(index: Int) = selectItem(this[index])
    suspend fun selectItem(itemId: Int) = selectItem(first(itemId))
    suspend fun selectItem(item: RSItem?): Boolean {
        if(item == null || item.id == -1 || item.id == EMPTY_SLOT_ITEM_ID) {
            return false
        }

        if(selectedItem != null && selectedItem!!.id == item.id) {
            return true
        }

        if(open() && !item.interactWith("Use")) {
            return false
        }

        delay(75)
        return selectedItem != null && selectedItem!!.id == item.id
    }

    suspend fun useIndexOnIndex(index: Int, target: Int) = useItemOnItem(this[index], this[target])
    suspend fun useItemOnItem(itemId: Int, targetId: Int) = useItemOnItem(first(itemId), first(targetId))
    suspend fun useItemOnItem(item: RSItem?, target: RSItem?): Boolean {
        if(item == null || target == null || !open()) return false
        return selectItem(item) && target.interactWith("Use")
    }

    suspend fun useIndexOnObject(index: Int, target: RSObject?) = useItemOnObject(this[index], target)
    suspend fun useItemOnObject(itemId: Int, target: RSObject?) = useItemOnObject(first(itemId), target)
    suspend fun useItemOnObject(item: RSItem?, target: RSObject?): Boolean {
        if(item == null || target == null || !open()) return false
        return selectItem(item) && target.interactWith("Use")
    }

    suspend fun useIndexOnNpc(index: Int, target: RSNPC?) = useItemOnNpc(this[index], target)
    suspend fun useItemOnNpc(itemId: Int, target: RSNPC?) = useItemOnNpc(first(itemId), target)
    suspend fun useItemOnNpc(item: RSItem?, target: RSNPC?): Boolean {
        if(item == null || target == null || !open()) return false
        return selectItem(item) && target.interactWith("Use")
    }

    companion object {
        private const val EMPTY_SLOT_ITEM_ID: Int = 6512
        private const val KEY_INVENTORY: String = "inventory"
        private const val KEY_BANK: String = "bank"
        private const val KEY_STORE: String = "store"
        private const val KEY_GE: String = "grandexchange"
        private const val KEY_TRADE: String = "trade"

        private fun isOpen(widget: RSWidget): Boolean {
            return widget.isValid && widget.isVisible()
        }
    }
}