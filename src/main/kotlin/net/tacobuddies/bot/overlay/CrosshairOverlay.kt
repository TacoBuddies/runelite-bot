package net.tacobuddies.bot.overlay

import net.runelite.api.Client
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.OverlayLayer
import net.runelite.client.ui.overlay.OverlayPosition
import net.tacobuddies.bot.BotConfig
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import javax.inject.Inject

class CrosshairOverlay : Overlay() {

    @Inject
    private lateinit var client: Client

    @Inject
    private lateinit var config: BotConfig

    private val color: Color = Color(255, 255, 255, 100)

    init {
        position = OverlayPosition.DYNAMIC
        layer = OverlayLayer.ALWAYS_ON_TOP
    }

    override fun render(g: Graphics2D): Dimension? {
        if(config.renderCrosshairOverlay()) {
            val mousePos = client.mouseCanvasPosition

            g.color = color
            g.drawLine(0, mousePos.y, client.canvasWidth, mousePos.y)
            g.drawLine(mousePos.x, 0, mousePos.x, client.canvasHeight)
        }
        return null
    }
}