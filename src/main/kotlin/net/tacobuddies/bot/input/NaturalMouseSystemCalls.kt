package net.tacobuddies.bot.input

import com.github.joonasvali.naturalmouse.api.SystemCalls
import net.tacobuddies.bot.helpers.Mouse.Companion.SOURCE
import net.tacobuddies.bot.scriptable.TaskContext
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.MouseEvent

class NaturalMouseSystemCalls(private val context: TaskContext) : SystemCalls {
    override fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    override fun sleep(p0: Long) {
        try { Thread.sleep(p0) } catch(ignored: InterruptedException) {}
    }

    override fun getScreenSize(): Dimension {
        return Toolkit.getDefaultToolkit().screenSize
    }

    override fun setMousePosition(x: Int, y: Int) {
        var destX = x
        var destY = y

        if(x < 0 || x > context.client.canvasWidth)
            destX = -1

        if(y < 0 || y > context.client.canvasHeight)
            destY = -1

        context.client.canvas.mouseMotionListeners.forEach {
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
}