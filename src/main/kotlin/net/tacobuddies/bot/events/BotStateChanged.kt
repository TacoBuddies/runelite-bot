package net.tacobuddies.bot.events

import lombok.Value
import net.tacobuddies.bot.BotState

@Value
data class BotStateChanged(val state: BotState)
