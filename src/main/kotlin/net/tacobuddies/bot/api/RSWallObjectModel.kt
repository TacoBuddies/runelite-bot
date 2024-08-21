package net.tacobuddies.bot.api

import net.runelite.api.Model
import net.runelite.api.WallObject
import net.tacobuddies.bot.scriptable.ScriptContext

class RSWallObjectModel(context: ScriptContext, model: Model?, val `object`: WallObject) : RSModel(context, model) {
    constructor(context: ScriptContext, model: Model?, model2: Model?, `object`: WallObject) : this(context, model, `object`)

    override val localX: Int
        get() = `object`.localLocation.x

    override val localY: Int
        get() = `object`.localLocation.y

    // Do nothing
    override fun update() {}
}