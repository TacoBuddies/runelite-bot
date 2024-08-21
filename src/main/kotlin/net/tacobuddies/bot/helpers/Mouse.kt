package net.tacobuddies.bot.helpers

import net.runelite.api.Point
import net.tacobuddies.bot.input.DumbMouse
import net.tacobuddies.bot.input.Mouse
import net.tacobuddies.bot.input.NaturalMouse
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.TaskContext
import java.awt.Canvas
import java.awt.Component

class Mouse(context: TaskContext) : ContextProvider<TaskContext>(context) {
    private val client = context.client
    private val virtualMouse: Mouse by lazy { if(smartMouse) { NaturalMouse(context) } else { DumbMouse(context) } }

    suspend fun clickAt(point: Point, rightClick: Boolean) {
        virtualMouse.clickAt(point, rightClick)
    }

    suspend fun clickAt(x: Int, y: Int, rightClick: Boolean) {
        virtualMouse.clickAt(x, y, rightClick)
    }

    suspend fun dragTo(point: Point) {
        virtualMouse.dragTo(point)
    }

    suspend fun dragTo(x: Int, y: Int) {
        virtualMouse.dragTo(x, y)
    }

    suspend fun moveMouseRandomly(randomX: Int, randomY: Int) {
        var thisX = client.mouseCanvasPosition.x
        var thisY = client.mouseCanvasPosition.y
        virtualMouse.moveTo(thisX + 1, thisY + 1, randomX, randomY)
    }

    suspend fun moveTo(point: Point) {
        virtualMouse.moveTo(point)
    }

    suspend fun moveTo(point: Point, randomX: Int, randomY: Int) {
        virtualMouse.moveTo(point, randomX, randomY)
    }

    suspend fun moveTo(x: Int, y: Int) {
        virtualMouse.moveTo(x, y)
    }

    companion object {
        val SOURCE: Component = Canvas()
        val smartMouse = System.getProperty("net.tacobuddies.smartMouse", "false").toBoolean()
    }
}