package net.tacobuddies.bot.randoms.vulcan

import kotlinx.coroutines.delay
import net.tacobuddies.bot.api.RSNPC
import net.tacobuddies.bot.scriptable.RandomEvent
import net.tacobuddies.bot.scriptable.ScriptContext

@Suppress("unused")
class VulcanPilloryGuard : RandomEvent() {

    override fun shouldActivate(context: ScriptContext): Boolean {
        return getGuard(context) != null
    }

    override suspend fun run(context: ScriptContext) {
        val npc = getGuard(context) ?: return

        if(npc.interactWith("Talk-to")) {
            delay(1200)
            while(context.interfaces.canContinue()) {
                context.interfaces.clickContinue()
            }
            delay(1200)
        }
    }

    private fun getGuard(context: ScriptContext): RSNPC? {
        val localPlayer = context.players.localPlayer
        val localPlayerName = localPlayer.name ?: "null"
        val guard = context.npcs.getNearest {
            it.npc.id == NPC_PILLORY_GUARD
                    && (it.npc.overheadText.contains(localPlayerName, ignoreCase = true)
                        || it.npc.interacting == context.client.localPlayer)
        }

        return guard
    }

    companion object {
        const val NPC_PILLORY_GUARD: Int = 380
    }
}