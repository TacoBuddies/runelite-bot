package net.tacobuddies.bot.helpers

import net.tacobuddies.bot.api.RSPlayer
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext

class Players(context: ScriptContext) : ContextProvider<ScriptContext>(context) {

    val localPlayer: RSPlayer
        get() = RSPlayer(context, context.client.localPlayer)
}