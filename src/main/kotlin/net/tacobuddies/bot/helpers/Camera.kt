package net.tacobuddies.bot.helpers

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldPoint
import net.tacobuddies.bot.api.RSTile
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import java.awt.event.KeyEvent
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}
class Camera(context: ScriptContext) : ContextProvider<ScriptContext>(context) {
    private val client = context.client

    val pitch: Int
        get() = ((context.client.cameraPitch - 1024) / 20.48).toInt()

    val angle: Int
        get() = abs(context.client.cameraYaw / 45.51 * 8).toInt()

    suspend fun lookAt(tile: RSTile?) {
        if(tile != null)
            lookAt(tile.worldLocation)
    }

    suspend fun lookAt(point: WorldPoint) {
        val localPos = client.localPlayer.worldLocation
        val dX = point.x - localPos.x
        val dY = point.y - localPos.y
        var angle = Math.toDegrees(atan2(dY.toDouble(), dX.toDouble()))
        if(angle < 0) {
            angle += 360
        }

        angle = (angle - 90) % 360
        setAngle(angle.toInt())
    }

    suspend fun lookAt(point: LocalPoint) = lookAt(WorldPoint.fromLocal(client, point))

    suspend fun setPitch(up: Boolean): Boolean {
        return setPitch(if(up) { 100 } else { 0 })
    }

    suspend fun setPitch(percent: Int): Boolean {
        var curAlt = pitch
        var lastAlt = 0
        if(curAlt == percent) {
            return true
        } else if(curAlt < percent) {
            context.keyboard.pressKey(KeyEvent.VK_UP.toChar())
            var start = System.currentTimeMillis()
            while(curAlt < percent && System.currentTimeMillis() - start < random(50, 100)) {
                if(lastAlt != curAlt) {
                    start = System.currentTimeMillis()
                }
                lastAlt = curAlt
                delay(random(5, 10).toDuration(DurationUnit.MILLISECONDS))
                curAlt = pitch
            }
            context.keyboard.releaseKey(KeyEvent.VK_UP.toChar())
            return true;
        } else {
            context.keyboard.pressKey(KeyEvent.VK_DOWN.toChar())
            var start = System.currentTimeMillis()
            while(curAlt > percent && System.currentTimeMillis() - start < random(50, 100)) {
                if(lastAlt != curAlt) {
                    start = System.currentTimeMillis()
                }
                lastAlt = curAlt
                delay(random(5, 10).toDuration(DurationUnit.MILLISECONDS))
                curAlt = pitch
            }
            context.keyboard.releaseKey(KeyEvent.VK_DOWN.toChar())
            return true
        }
    }

    suspend fun setAngle(degrees: Int) {
        if(getAngleTo(degrees) > 5) {
            context.keyboard.pressKey(KeyEvent.VK_LEFT.toChar())
            while(getAngleTo(degrees) > 5) {
                delay(10)
            }
            context.keyboard.releaseKey(KeyEvent.VK_LEFT.toChar())
        } else if(getAngleTo(degrees) < -5) {
            context.keyboard.pressKey(KeyEvent.VK_RIGHT.toChar())
            while (getAngleTo(degrees) < -5) {
                delay(10)
            }
            context.keyboard.releaseKey(KeyEvent.VK_RIGHT.toChar())
        }
    }

    fun getAngleTo(degrees: Int): Int {
        var currentAngle = angle
        if(currentAngle < degrees) {
            currentAngle += 360
        }

        var deltaAngle = currentAngle - degrees
        if(deltaAngle > 180) {
            deltaAngle -= 360
        }
        return deltaAngle
    }
}