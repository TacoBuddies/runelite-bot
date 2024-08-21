package net.tacobuddies.bot.events

import lombok.Value
import net.tacobuddies.bot.BotState

@Value
data class RequestBotState(val state: BotState)