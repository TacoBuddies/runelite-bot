package net.tacobuddies.scripts.debug

import net.tacobuddies.bot.scriptable.Script
import net.tacobuddies.bot.scriptable.ScriptDescriptor

@ScriptDescriptor(
    name = "Keyboard Test",
    enabled = false
)
class TestKeyboard : Script() {
    override fun onStart(): Boolean {
        return true
    }

    override fun onStop() {
    }

    override suspend fun loop(): Int {
        keyboard.sendKeys("Fku Steven", true)
        return 1000
    }
}