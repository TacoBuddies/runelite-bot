package net.tacobuddies.bot.helpers

import kotlinx.coroutines.delay
import net.runelite.api.Varbits
import net.runelite.api.widgets.WidgetInfo
import net.tacobuddies.bot.api.RSItem
import net.tacobuddies.bot.api.RSTile
import net.tacobuddies.bot.api.RSWidget
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext

class Bank(context: ScriptContext) : ContextProvider<ScriptContext>(context) {

    val `interface`: RSWidget
        get() = context.interfaces.get(WidgetInfo.BANK_CONTAINER.groupId)

    val depositInterface: RSWidget
        get() = context.interfaces.get(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER.groupId)

    val isOpen: Boolean
        get() = `interface`.isValid && `interface`.isVisible()

    val isDepositOpen: Boolean
        get() = depositInterface.isValid && depositInterface.isVisible()

    val rearrangeMode: RearrangeMode?
        get() {
            if(!isOpen) return null
            val varp = context.client.getVarbitValue(Varbits.BANK_REARRANGE_MODE)
            return RearrangeMode.entries[varp]
        }

    val withdrawalMode: WithdrawalMode?
        get() {
            if(!isOpen) return null
            val varp = context.client.getVarbitValue(6590)
            return WithdrawalMode.entries[varp]
        }

    val availableBankSpace: Int
        get() = -1

    val maxBankSpace: Int
        get() = -1

    val currentBankTab: Int
        get() = if(isOpen) context.client.getVarbitValue(Varbits.CURRENT_BANK_TAB) else -1

    val items: List<RSItem>
        get() {
            val bankInterface = context.interfaces.getComponent(WidgetInfo.BANK_ITEM_CONTAINER)
            if(!isOpen || !bankInterface.isValid) {
                return emptyList()
            }

            val components = bankInterface.getComponents()
            val items = mutableListOf<RSItem>()
            for(comp in components) {
                items.add(RSItem(context, comp))
            }

            return items.toList()
        }

    operator fun get(index: Int): RSItem? {
        for(item in items) {
            val id = item.component?.id ?: continue
            if(WidgetInfo.TO_CHILD(id) == index) {
                return item
            }
        }
        return null
    }

    fun first(vararg ids: Int): RSItem? {
        return items
            .firstOrNull { it.id in ids }
    }

    fun contains(vararg ids: Int): Boolean {
        return items
            .any { it.id in ids }
    }

    fun getTabWidget(index: Int): RSWidget {
        return context.interfaces.getComponent(WidgetInfo.BANK_TAB_CONTAINER).getComponent(index)
    }

    suspend fun open(): Boolean {
        if(isOpen) {
            return true
        }

        var nearestBank = context.objects.getNearest { it.hasAction("Bank") || it.hasAction("Use-Quickly") }
        val nearestBanker = context.npcs.getNearest { it.hasAction("Bank") }

        var lowestDist = Int.MAX_VALUE
        var tile: RSTile? = null
        if(nearestBank != null) {
            tile = nearestBank.location
            lowestDist = context.players.localPlayer.distanceTo(tile)
        }
        if(nearestBanker != null && context.players.localPlayer.distanceTo(nearestBanker) < lowestDist) {
            tile = nearestBanker.location
            lowestDist = context.players.localPlayer.distanceTo(nearestBanker)
            nearestBank = null
        }

        if(lowestDist < 5 && context.calculations.tileOnMap(tile) && context.calculations.canReach(tile, true)) {
            var didAction = false
            if(nearestBank != null) {
                didAction = nearestBank.interactWith("Bank") || nearestBank.interactWith("Use-Quickly")
            } else if(nearestBanker != null) {
                didAction = nearestBanker.interactWith("Bank")
            }

            if(didAction) {
                var count = 0
                while(!isOpen && ++count < 10) {
                    delay(300)
                    if(context.players.localPlayer.isLocalPlayerMoving) {
                        count = 0
                    }
                }
            } else {
                context.camera.lookAt(tile)
            }
        }
        return isOpen
    }

    suspend fun switchToTab(index: Int): Boolean {
        if(!isOpen || isDepositOpen) {
            return false
        }

        if(currentBankTab == index) {
            return true
        }

        val widget = getTabWidget(index)
        return widget.isValid && widget.clickMouse(false)
    }

    suspend fun setRearrangeMode(mode: RearrangeMode): Boolean {
        if(!isOpen) {
            return false
        }

        if(rearrangeMode != mode) {
            val widget = context.interfaces.getComponent(12, mode.childId)
            widget.clickMouse(false)
            delay(600)
        }

        return rearrangeMode == mode
    }

    suspend fun setWithdrawalMode(mode: WithdrawalMode): Boolean {
        if(!isOpen) {
            return false
        }

        if(withdrawalMode != mode) {
            val widget = context.interfaces.getComponent(12, mode.childId)
            widget.clickMouse(false)
            delay(600)
        }

        return withdrawalMode == mode
    }

    suspend fun close(): Boolean {
        if(isOpen) {
            val bankContainer = context.interfaces.getComponent(WidgetInfo.BANK_CONTAINER.groupId, 2)
            val closeButton = bankContainer.getDynamicComponent(BANK_CLOSE_BUTTON) ?: return false
            closeButton.clickMouse(false)
            delay(500)
            return !isOpen
        }

        if(isDepositOpen) {
            val depositContainer = context.interfaces.getComponent(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER.groupId, 1)
            val closeButton = depositContainer.getDynamicComponent(DEPOSIT_CLOSE_BUTTON) ?: return false
            closeButton.clickMouse(false)
            delay(500)
            return !isDepositOpen
        }

        return false
    }

    suspend fun depositAll(): Boolean {
        if(isOpen) {
            return context.interfaces.getComponent(WidgetInfo.BANK_DEPOSIT_INVENTORY).clickMouse(false)
        } else if(isDepositOpen) {
            return context.interfaces.getComponent(192, 4).clickMouse(false)
        }
        return false
    }

    suspend fun depositEquipment(): Boolean {
        if(isOpen) {
            return context.interfaces.getComponent(WidgetInfo.BANK_DEPOSIT_EQUIPMENT).clickMouse(false)
        } else if(isDepositOpen) {
            return context.interfaces.getComponent(192, 6).clickMouse(false)
        }
        return false
    }

    enum class RearrangeMode(val childId: Int) {
        Swap(17), Insert(19)
    }

    enum class WithdrawalMode(val childId: Int) {
        Item(22), Note(24)
    }

    companion object {
        const val BANK_CLOSE_BUTTON: Int = 11
        const val DEPOSIT_CLOSE_BUTTON: Int = 11
    }
}