package net.tacobuddies.bot.api

import net.runelite.api.NPC
import net.tacobuddies.bot.scriptable.ScriptContext

class RSNPC(context: ScriptContext, val npc: NPC) : RSActor(context, npc) {
    private val definition = context.npcManager.get(npc.id)

    val name: String
        get() = definition?.name ?: ""

    val actions: Array<String>
        get() = definition?.actions ?: emptyArray()

    fun hasAction(action: String): Boolean {
        for(a in actions) {
            if(a.equals(action, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    suspend fun interactWith(action: String): Boolean {
        return super.interactWith(action, null, npc.index)
    }
}