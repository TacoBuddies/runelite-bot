package net.tacobuddies.bot.overlay

import net.runelite.api.Client
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.OverlayLayer
import net.runelite.client.ui.overlay.OverlayPosition
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import javax.inject.Inject

class MousePositionOverlay : Overlay() {

    @Inject
    lateinit var client: Client

    init {
        this.position = OverlayPosition.DYNAMIC
        this.layer = OverlayLayer.ALWAYS_ON_TOP
    }

    override fun render(g: Graphics2D): Dimension? {
        val pos = client.mouseCanvasPosition

        g.color = Color.WHITE
        g.drawString("X: ${pos.x}, Y: ${pos.y}", 15, 15)

        return null
    }
}