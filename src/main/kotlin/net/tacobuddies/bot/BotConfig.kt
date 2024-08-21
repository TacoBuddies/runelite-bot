package net.tacobuddies.bot

import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem

@ConfigGroup("bot")
@JvmDefaultWithCompatibility
interface BotConfig : Config {
    @ConfigItem(
        keyName = "renderCrosshairOverlay",
        name = "Render Crosshair",
        description = "Show the crosshair overlay"
    )
    fun renderCrosshairOverlay(): Boolean = true

    @ConfigItem(
        keyName = "loginAfterDisconnect",
        name = "Login after Disconnect",
        description = "Login again after total disconnections"
    )
    fun loginAfterDisconnect(): Boolean = true
}