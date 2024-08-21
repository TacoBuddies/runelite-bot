package net.tacobuddies.bot.api

import net.runelite.api.Model
import net.runelite.api.Perspective
import net.runelite.api.Tile
import net.runelite.api.coords.LocalPoint
import net.tacobuddies.bot.scriptable.ScriptContext

class RSGroundObjectModel(context: ScriptContext, model: Model, private val tile: Tile) : RSModel(context, model) {
    override val localX: Int
        get() = tile.localLocation.x

    override val localY: Int
        get() = tile.localLocation.y

    override val localZ: Int
        get() {
            val itemLayer = tile.itemLayer
            if(itemLayer != null) {
                return itemLayer.z - itemLayer.height
            } else {
                return Perspective.getTileHeight(context.client, LocalPoint(localX, localY), context.client.plane)
            }
        }

    // Do Nothing
    override fun update() {}
}