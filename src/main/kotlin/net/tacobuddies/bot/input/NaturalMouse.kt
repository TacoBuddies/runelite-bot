package net.tacobuddies.bot.input

import com.github.joonasvali.naturalmouse.support.*
import com.github.joonasvali.naturalmouse.util.FactoryTemplates
import com.github.joonasvali.naturalmouse.util.FlowTemplates
import kotlinx.coroutines.delay
import net.runelite.api.Point
import net.tacobuddies.bot.helpers.Mouse.Companion.SOURCE
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.TaskContext
import java.awt.event.MouseEvent
import java.util.Random

class NaturalMouse(context: TaskContext) : ContextProvider<TaskContext>(context), Mouse {
    private val client = context.client

    private val nature = MouseMotionNature().apply {
        systemCalls = NaturalMouseSystemCalls(context)
        deviationProvider = SinusoidalDeviationProvider(10.0)
        noiseProvider = DefaultNoiseProvider(2.0)
        speedManager = DefaultSpeedManager(listOf(Flow(FlowTemplates.constantSpeed())))
        overshootManager = DefaultOvershootManager(Random()).apply { overshoots = 1 }
        effectFadeSteps = 15
        mouseInfo = DefaultMouseInfoAccessor()
        reactionTimeBaseMs = 10
        reactionTimeVariationMs = 20
        minSteps = 10
    }

    private val motionFactory = FactoryTemplates.createFastGamerMotionFactory(nature).apply {
        setMouseInfo { java.awt.Point(context.client.mouseCanvasPosition.x, context.client.mouseCanvasPosition.y) }
    }

    override suspend fun moveTo(x: Int, y: Int, dX: Int, dY: Int) {
        motionFactory.move(x, y)
    }

    override suspend fun moveTo(point: Point, dX: Int, dY: Int) {
        moveTo(point.x, point.y)
    }

    override suspend fun moveTo(x: Int, y: Int) {
        moveTo(x, y, 0, 0)
    }

    override suspend fun moveTo(point: Point) {
        moveTo(point.x, point.y)
    }

    override suspend fun clickAt(x: Int, y: Int, rightClick: Boolean) {
        moveTo(x, y)
        mousePress(x, y, rightClick)
        delay(50)
        mouseRelease(x, y, rightClick)
        delay(50)
    }

    override suspend fun clickAt(point: Point, rightClick: Boolean) {
        clickAt(point.x, point.y, rightClick)
    }

    override suspend fun dragTo(x: Int, y: Int) {
        val current = client.mouseCanvasPosition
        mousePress(current.x, current.y, false)
        delay(250)
        moveTo(x, y)
        delay(250)
        mouseRelease(x, y, false)
    }

    override suspend fun dragTo(point: Point) {
        dragTo(point.x, point.y)
    }

    private fun mousePress(x: Int, y: Int, rightClick: Boolean) {
        client.canvas.mouseListeners.forEach {
            it.mousePressed(
                MouseEvent(
                SOURCE,
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(),
                0,
                x,
                y,
                0,
                false,
                if(rightClick) { MouseEvent.BUTTON3 } else { MouseEvent.BUTTON1 }
            )
            )
        }
    }

    private fun mouseRelease(x: Int, y: Int, rightClick: Boolean) {
        client.canvas.mouseListeners.forEach {
            it.mouseReleased(
                MouseEvent(
                SOURCE,
                MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(),
                0,
                x,
                y,
                0,
                false,
                if(rightClick) { MouseEvent.BUTTON3 } else { MouseEvent.BUTTON1 }
            )
            )
        }
    }
}