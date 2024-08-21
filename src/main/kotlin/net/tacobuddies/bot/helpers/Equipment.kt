package net.tacobuddies.bot.helpers

import net.runelite.api.EquipmentInventorySlot
import net.runelite.api.InventoryID
import net.runelite.api.widgets.WidgetInfo
import net.tacobuddies.bot.api.RSItem
import net.tacobuddies.bot.api.RSWidget
import net.tacobuddies.bot.api.constants.InterfaceTab
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext

class Equipment(context: ScriptContext) : ContextProvider<ScriptContext>(context) {

    val `interface`: RSWidget?
        get() {
            val widget = context.client.getWidget(EQUIPMENT_INTERFACE) ?: return null
            return RSWidget(context, widget)
        }

    val isOpen: Boolean
        get() = context.game.currentTab == InterfaceTab.Equipment

    val items: List<RSItem?>
        get() {
            val container = context.client.getItemContainer(InventoryID.EQUIPMENT) ?: return emptyList()
            val items = container.items

            val equipInter = `interface` ?: return emptyList()

            val out = MutableList<RSItem?>(container.size()) { null }
            for(i in items.indices) {
                if(items[i].id != -1) {
                    val slot = slotFromIndex(i)
                    val widget = equipInter.getComponent(slot.childId).getDynamicComponent(1) ?: continue
                    out[i] = RSItem(context, widget, items[i])
                }
            }

            return out.toList()
        }

    operator fun get(index: Int): RSItem? {
        val container = context.client.getItemContainer(InventoryID.EQUIPMENT) ?: return null
        if(index < 0 || index >= container.size()) {
            return null
        }

        val item = container.getItem(index) ?: return null
        if(item.id == -1) {
            return null
        }

        val slot = slotFromIndex(index)
        val widget = `interface`?.getComponent(slot.childId) ?: return null
        return RSItem(context, widget, item)
    }

    operator fun get(index: Slot): RSItem? {
        return get(index.index)
    }

    operator fun get(index: EquipmentInventorySlot): RSItem? {
        return get(index.slotIdx)
    }

    fun contains(vararg ids: Int): Boolean {
        return items
            .filterNotNull()
            .any { it.id in ids }
    }

    suspend fun open(): Boolean {
        return context.game.openTab(InterfaceTab.Equipment)
    }

    enum class Slot(val index: Int, val childId: Int) {
        Head(0, 15),
        Cape(1, 16),
        Amulet(2, 17),
        Weapon(3, 18),
        Body(4, 19),
        Shield(5, 20),
        Legs(7, 21),
        Gloves(9, 22),
        Boots(10, 23),
        Ring(12, 24),
        Ammo(13, 25)
    }

    companion object {
        val EQUIPMENT_INTERFACE: WidgetInfo = WidgetInfo.EQUIPMENT

        private fun slotFromIndex(index: Int): Slot {
            return Slot.entries.first { it.index == index }
        }
    }
}