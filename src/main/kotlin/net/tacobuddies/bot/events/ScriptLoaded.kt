package net.tacobuddies.bot.events

import lombok.Value
import net.tacobuddies.bot.scriptable.Script

@Value
data class ScriptLoaded(val script: Script)
