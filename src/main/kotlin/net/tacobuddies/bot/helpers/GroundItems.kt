package net.tacobuddies.bot.helpers

import net.runelite.api.Node
import net.runelite.api.TileItem
import net.runelite.api.coords.WorldPoint
import net.tacobuddies.bot.api.RSGroundItem
import net.tacobuddies.bot.api.RSTile
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import java.util.function.Predicate

class GroundItems(context: ScriptContext) : ContextProvider<ScriptContext>(context) {
    private val client = context.client

    fun getAll(): List<RSGroundItem> {
        return getAll(DEFAULT_SEARCH_RANGE) { true }
    }

    fun getAll(range: Int, predicate: Predicate<RSGroundItem>): List<RSGroundItem> {
        val out = mutableListOf<RSGroundItem>()
        val pX = client.localPlayer.worldLocation.x
        val pY = client.localPlayer.worldLocation.y
        val minX = pX - range
        val minY = pY - range
        val maxX = pX + range
        val maxY = pY + range
        for(x in minX until maxX) {
            for(y in minY until maxY) {
                val items = getAllAt(x, y)
                for(item in items) {
                    if(predicate.test(item)) {
                        out.add(item)
                    }
                }
            }
        }
        return out.toList()
    }

    fun getNearest(predicate: Predicate<RSGroundItem>): RSGroundItem? {
        var dist = Int.MAX_VALUE
        val playerLoc = client.localPlayer.worldLocation
        val pX = playerLoc.x
        val pY = playerLoc.y
        val minX = pX - DEFAULT_SEARCH_RANGE
        val minY = pY - DEFAULT_SEARCH_RANGE
        val maxX = pX + DEFAULT_SEARCH_RANGE
        val maxY = pY + DEFAULT_SEARCH_RANGE

        var out: RSGroundItem? = null
        for(x in minX until maxX) {
            for(y in minY until maxY) {
                val items = getAllAt(x, y)
                for(item in items) {
                    if(predicate.test(item) && playerLoc.distanceTo(item.tile.worldLocation) < dist) {
                        dist = playerLoc.distanceTo(item.tile.worldLocation)
                        out = item
                    }
                }
            }
        }
        return out
    }

    fun getNearestById(vararg ids: Int): RSGroundItem? {
        return getNearest {
            for(id in ids) {
                if(id == it.item.id) {
                    return@getNearest true
                }
            }
            return@getNearest false
        }
    }

    private fun getAllAt(x: Int, y: Int): List<RSGroundItem> {
        val out = mutableListOf<RSGroundItem>()

        val tile = context.calculations.getTile(WorldPoint(x, y, client.plane)) ?: return emptyList()
        val rsTile = RSTile(context, tile)

        val itemLayer = tile.itemLayer ?: return emptyList()
        var current: Node = itemLayer.top
        while(current is TileItem) {
            out.add(RSGroundItem(context, rsTile, current))
            current = current.next
        }

        return out.toList()
    }

    companion object {
        const val DEFAULT_SEARCH_RANGE: Int = 25
    }
}