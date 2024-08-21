package net.tacobuddies.bot.api

import net.runelite.api.GameObject
import net.runelite.api.Model
import net.tacobuddies.bot.scriptable.ScriptContext

class RSObjectModel(context: ScriptContext, model: Model, val `object`: GameObject) : RSModel(context, model) {

    override val localX: Int
        get() = `object`.x

    override val localY: Int
        get() = `object`.y

    override val orientation: Int
        get() = `object`.orientation

    // Do nothing
    override fun update() {}
}