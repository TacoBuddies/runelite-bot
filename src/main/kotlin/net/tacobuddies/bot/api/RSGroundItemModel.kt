package net.tacobuddies.bot.api

import net.runelite.api.Model
import net.runelite.api.Tile
import net.tacobuddies.bot.scriptable.ScriptContext

class RSGroundItemModel(context: ScriptContext, model: Model, val tile: Tile) : RSModel(context, model) {
    override val localX: Int
        get() = tile.localLocation.x

    override val localY: Int
        get() = tile.localLocation.y

    // Do nothing
    override fun update() {}
}