package net.tacobuddies.bot.events

import lombok.Value
import java.io.File

@Value
data class RequestScriptLoad(val file: File)
