package net.tacobuddies.bot.input

import kotlinx.coroutines.delay
import net.runelite.api.Point
import net.tacobuddies.bot.helpers.Mouse.Companion.SOURCE
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.TaskContext
import java.awt.event.MouseEvent
import kotlin.random.Random

class DumbMouse(context: TaskContext) : ContextProvider<TaskContext>(context), Mouse {
    val client = context.client

    override suspend fun moveTo(x: Int, y: Int, dX: Int, dY: Int) {
        var thisX = client.mouseCanvasPosition.x
        var thisY = client.mouseCanvasPosition.y

        if(thisX < 0 || thisY < 0) {
            // If outside of canvas, pick a random side to enter
            when(Random.nextInt(1, 5)) {
                1 -> { // Enter from left side
                    thisX = 1
                    thisY = Random.nextInt(1, client.canvasHeight)
                }
                2 -> { // Enter from bottom side
                    thisX = Random.nextInt(1, client.canvasWidth)
                    thisY = client.canvasHeight - 1
                }
                3 -> { // Enter from right side
                    thisX = client.canvasWidth - 1
                    thisY = Random.nextInt(1, client.canvasHeight)
                }
                4 -> { // Enter from top side
                    thisX = Random.nextInt(1, client.canvasWidth)
                    thisY = 1
                }
            }
            setMousePosition(thisX, thisY)
        }

        if(thisX == x && thisY == y) {
            return
        }

        // If randomness provided, move within parameters
        val rX = if(dX == 0) { x } else { Random.nextInt(x, x + dX) }
        val rY = if(dY == 0) { y } else { Random.nextInt(y, y + dY) }

        setMousePosition(rX, rY)
        delay(10)
    }

    override suspend fun moveTo(point: Point, dX: Int, dY: Int) {
        moveTo(point.x, point.y, dX, dY)
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
        delay(10)
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

    private fun setMousePosition(x: Int, y: Int) {
        var destX = x
        var destY = y

        if(x < 0 || x > client.canvasWidth)
            destX = -1

        if(y < 0 || y > client.canvasHeight)
            destY = -1

        client.canvas.mouseMotionListeners.forEach {
            it.mouseMoved(
                MouseEvent(
                SOURCE,
                MouseEvent.MOUSE_MOVED,
                System.currentTimeMillis(),
                0,
                destX,
                destY,
                0,
                false,
                0
            )
            )
        }
    }

    private fun mousePress(x: Int, y: Int, rightClick: Boolean) {
        client.canvas.mouseListeners.forEach {
            it.mousePressed(MouseEvent(
                SOURCE,
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(),
                0,
                x,
                y,
                0,
                false,
                if(rightClick) { MouseEvent.BUTTON3 } else { MouseEvent.BUTTON1 }
            ))
        }
    }

    private fun mouseRelease(x: Int, y: Int, rightClick: Boolean) {
        client.canvas.mouseListeners.forEach {
            it.mouseReleased(MouseEvent(
                SOURCE,
                MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(),
                0,
                x,
                y,
                0,
                false,
                if(rightClick) { MouseEvent.BUTTON3 } else { MouseEvent.BUTTON1 }
            ))
        }
    }
}