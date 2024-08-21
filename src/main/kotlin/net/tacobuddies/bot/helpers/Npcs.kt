package net.tacobuddies.bot.helpers

import net.runelite.api.NPC
import net.tacobuddies.bot.api.RSNPC
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext

class Npcs(context: ScriptContext) : ContextProvider<ScriptContext>(context) {
    private val client = context.client

    fun getNPCsById(id: Int): List<RSNPC> {
        return client.npcs
            .filter { it.id == id }
            .map { RSNPC(context, it) }
    }

    fun getClosestNPCById(vararg ids: Int): RSNPC? {
        val myPos = client.localPlayer.worldLocation
        val npc = client.npcs
            .filter { npc -> ids.contains(npc.id) }
            .minByOrNull { it.worldLocation.distanceTo(myPos) } ?: return null
        return RSNPC(context, npc)
    }

    fun getClosestNPCByIndex(vararg indices: Int): RSNPC? {
        val myPos = client.localPlayer.worldLocation
        val npc = client.npcs
            .filter { npc -> indices.contains(npc.index) }
            .minByOrNull { it.worldLocation.distanceTo(myPos) } ?: return null
        return RSNPC(context, npc)
    }

    fun getClosestNPCById(vararg ids: Int, predicate: (NPC) -> Boolean): RSNPC? {
        val myPos = client.localPlayer.worldLocation
        val npc = client.npcs
            .filter { npc -> ids.contains(npc.id) }
            .filter(predicate)
            .minByOrNull { it.worldLocation.distanceTo(myPos) } ?: return null
        return RSNPC(context, npc)
    }

    fun getClosestNPCByIndex(vararg indices: Int, predicate: (NPC) -> Boolean): RSNPC? {
        val myPos = client.localPlayer.worldLocation
        val npc = client.npcs
            .filter { npc -> indices.contains(npc.index) }
            .filter(predicate)
            .minByOrNull { it.worldLocation.distanceTo(myPos) } ?: return null
        return RSNPC(context, npc)
    }

    fun getNearest(predicate: (RSNPC) -> Boolean): RSNPC? {
        val myPos = client.localPlayer.worldLocation
        return client.npcs
            .map { RSNPC(context, it) }
            .filter(predicate)
            .minByOrNull { it.npc.worldLocation.distanceTo(myPos) }
    }
}