package net.tacobuddies.bot.tasks

import net.tacobuddies.bot.scriptable.ScriptContext

interface ScriptTask {
    suspend fun run(context: ScriptContext)
}