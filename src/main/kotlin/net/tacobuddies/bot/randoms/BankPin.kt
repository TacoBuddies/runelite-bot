package net.tacobuddies.bot.randoms

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import net.runelite.api.widgets.WidgetInfo
import net.runelite.api.widgets.WidgetType
import net.tacobuddies.bot.api.RSWidget
import net.tacobuddies.bot.scriptable.RandomEvent
import net.tacobuddies.bot.scriptable.ScriptContext

private val logger = KotlinLogging.logger {}

@Suppress("unused")
class BankPin : RandomEvent() {

    override fun shouldActivate(context: ScriptContext): Boolean {
        val account = context.accountManager.getDefaultAccount() ?: return false
        if(account.bankPin == null || account.bankPin == "" || account.bankPin.length != 4) return false

        val container = context.interfaces.getComponent(BANK_PIN_CONTAINER)
        return container.isValid && container.isVisible()
    }

    override suspend fun run(context: ScriptContext) {
        //Slow down in the beginning. Starts so fast you can't
        //physically see the first digit being pressed
        delay(2000)

        //We already check if these exist in shouldActivate,
        //safe to assume non-null
        val account = context.accountManager.getDefaultAccount()!!
        val bankPin = account.bankPin!!

        for(num in bankPin) {
            val widget = getWidgetFor(context, num) ?: return
            val parent = widget.getParent()

            if(parent.isValid && parent.isVisible()) {
                parent.clickMouse(false)
            } else {
                widget.clickMouse(false)
            }
            context.mouse.moveMouseRandomly(200, 200)
            delay(1200)
        }
    }

    private fun getWidgetFor(context: ScriptContext, key: Char): RSWidget? {
        if(key < '0' || key > '9') {
            logger.warn { "Bank pin consisted of a character outside of [0-9]" }
            return null
        }

        val container = context.interfaces.getComponent(BANK_PIN_CONTAINER)
        if(!container.isValid || !container.isVisible()) {
            logger.warn { "Bank pin interface is no longer valid" }
            return null
        }

        for(comp in container.getComponents()) {
            if(comp.text == key.toString()) {
                return comp
            }
        }

        return null
    }

    companion object {
        private val BANK_PIN_CONTAINER = WidgetInfo.BANK_PIN_CONTAINER
    }
}