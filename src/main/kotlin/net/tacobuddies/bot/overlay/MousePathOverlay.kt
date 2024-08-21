package net.tacobuddies.bot.overlay

import com.github.otsoko.bezier.LinearBezierCurve
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.OverlayLayer
import net.runelite.client.ui.overlay.OverlayPosition
import net.tacobuddies.bot.events.MousePathChanged
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.geom.Point2D

class MousePathOverlay : Overlay() {
    private var curve: LinearBezierCurve? = null

    init {
        layer = OverlayLayer.ALWAYS_ON_TOP
        position = OverlayPosition.DYNAMIC
    }

    override fun render(g: Graphics2D): Dimension? {
        if(curve != null) {
            g.color = Color.GREEN

            val start = curve!!.start
            val end = curve!!.end

            val distance = Point2D.distance(start.x.toDouble(), start.y.toDouble(), end.x.toDouble(), end.y.toDouble()).toInt()

            var lastX = start.x
            var lastY = start.y
            val dt = 1.0f / (distance - 1)
            for(i in 0..distance) {
                val point = curve!!.getPointAtCurve(dt * i)
                g.drawLine(lastX.toInt(), lastY.toInt(), point.x.toInt(), point.y.toInt())
                lastX = point.x
                lastY = point.y
            }
        }
        return null
    }

    @Subscribe
    fun onMousePathChanged(event: MousePathChanged) {
        curve = event.curve as LinearBezierCurve
    }
}