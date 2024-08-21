package net.tacobuddies.bot.helpers

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import net.runelite.api.VarClientInt
import net.tacobuddies.bot.api.RSWidget
import net.tacobuddies.bot.api.constants.InterfaceTab
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}
class Game(context: ScriptContext) : ContextProvider<ScriptContext>(context) {

    val isLoggedIn: Boolean
        get() = context.client.localPlayer != null

    val plane: Int
        get() = context.client.plane

    val baseX: Int
        get() = context.client.baseX

    val baseY: Int
        get() = context.client.baseY

    val currentTab: InterfaceTab
        get() {
            val tab = context.client.getVarcIntValue(VarClientInt.INVENTORY_TAB)
            return if(tab == -1) {
                InterfaceTab.NotSelected
            } else
                InterfaceTab.entries[tab]
        }

    suspend fun openTab(tab: InterfaceTab): Boolean {
        if(currentTab == tab) {
            return true
        }

        val tabWidget = context.gameUI.getTab(tab) ?: return false
        val widget = RSWidget(context, tabWidget)
        widget.clickMouse(false)

        delay(random(200, 400).toDuration(DurationUnit.MILLISECONDS))
        return tab == currentTab
    }
}