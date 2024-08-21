package net.tacobuddies.bot.api

import net.runelite.api.Point
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import net.tacobuddies.bot.utils.ShapeUtils
import net.tacobuddies.bot.utils.StdRandom
import java.awt.Shape
import java.awt.geom.PathIterator
import kotlin.math.*

class Hitbox(context: ScriptContext, private val interactable: Interactable, private val ignoreBoundsCheck: Boolean) : ContextProvider<ScriptContext>(context), Interactable {
    private var lastShapeUpdate = 0L
    private var cachedShape: Shape? = null

    override suspend fun interactWith(action: String, target: String?, identifier: Int?): Boolean {
        for (i in 0 until 3) {
            if(!shapeContainsPoint(context.client.mouseCanvasPosition)) {
                hoverMouse()
            }

            if (context.menu.clickMenuAction(action, target, identifier)) {
                return true
            }
        }
        return false
    }

    override suspend fun clickMouse(rightClick: Boolean): Boolean {
        for(i in 0 until 3) {
            if(!shapeContainsPoint(context.client.mouseCanvasPosition)) {
                hoverMouse()
            }

            if(shapeContainsPoint(context.client.mouseCanvasPosition)) {
                context.mouse.clickAt(context.client.mouseCanvasPosition, rightClick)
                return true
            }
        }
        return false
    }

    override suspend fun hoverMouse(): Boolean {
        val point = getPointNearCenter() ?: getRandomPoint() ?: return false
        if(!ignoreBoundsCheck && !context.calculations.pointOnScreen(point)) {
            return false
        }

        for(i in 0 until 3) {
            if(!shapeContainsPoint(context.client.mouseCanvasPosition)) {
                context.mouse.moveTo(point)
            }

            if(shapeContainsPoint(context.client.mouseCanvasPosition)) {
                return true
            }
        }
        return false
    }

    override fun getHitboxShape(): Shape? {
        if(cachedShape == null || System.currentTimeMillis() - lastShapeUpdate > 50) {
            cachedShape = interactable.getHitboxShape()
            lastShapeUpdate = System.currentTimeMillis()
        }
        return cachedShape
    }

    override fun getHitbox(): Hitbox {
        return interactable.getHitbox()
    }

    private fun shapeContainsPoint(point: Point): Boolean {
        val shape = getHitboxShape() ?: return false
        return shape.contains(point.x.toDouble(), point.y.toDouble())
    }

    private fun getRandomPoint(): Point? {
        val shape = getHitboxShape() ?: return null
        val bounds = shape.bounds
        for(j in 0 until 100) {
            val x = bounds.x + StdRandom.uniform(bounds.width)
            val y = bounds.y + StdRandom.uniform(bounds.height)
            if(shape.contains(x.toDouble(), y.toDouble())) {
                return Point(x, y)
            }
        }
        return null
    }

    private fun getOffset(point: Point): Point? {
        val shape = getHitboxShape() ?: return null
        val bounds = shape.bounds
        val x = bounds.x + point.x
        val y = bounds.y + point.y
        if(shape.contains(x.toDouble(), y.toDouble())) {
            return Point(x, y)
        }
        return null
    }

    private fun getCenterPoint(): Point? {
        val shape = getHitboxShape() ?: return null
        return ShapeUtils.computeCenter(shape)
    }

    private fun getPointNearCenter(): Point? {
        return getPointNearCenter(5)
    }

    @Suppress("SameParameterValue")
    private fun getPointNearCenter(attempts: Int): Point? {
        val shape = getHitboxShape() ?: return null
        val center = getCenterPoint() ?: return null
        val centerDistFromEdge = getDistanceFromEdge(center)
        val centerX = center.x
        val centerY = center.y

        var bestPoint: Point? = null
        var bestDistance = Double.MAX_VALUE
        for(i in 0 until attempts) {
            for(j in 0 until 100) {
                val x = StdRandom.gaussian(centerX.toDouble(), (centerDistFromEdge) / 3.0)
                val y = StdRandom.gaussian(centerY.toDouble(), (centerDistFromEdge) / 3.0)
                if(shape.contains(x, y)) {
                    val distance = getDistanceFromEdge(x.toInt(), y.toInt())
                    if(!distance.isNaN() && distance < bestDistance) {
                        bestPoint = Point(x.toInt(), y.toInt())
                        bestDistance = distance
                    }
                    break
                }
            }
        }
        return bestPoint
    }

    private fun getDistanceFromEdge(x: Int, y: Int): Double {
        val shape = getHitboxShape() ?: return Double.NaN

        val pi = shape.getPathIterator(null, 0.1)
        val prevCoords = DoubleArray(6)
        val coords = DoubleArray(6)
        var minDistance = Double.MAX_VALUE
        while(!pi.isDone) {
            val s = pi.currentSegment(coords)
            when(s) {
                PathIterator.SEG_MOVETO -> {
                    prevCoords[0] = coords[0]
                    prevCoords[1] = coords[1]
                }

                PathIterator.SEG_LINETO -> {
                    val distance = distanceToLineSegment(x.toDouble(), y.toDouble(), prevCoords[0], prevCoords[1], coords[0], coords[1])
                    if(distance < minDistance) {
                        minDistance = distance
                    }
                    prevCoords[0] = coords[0]
                    prevCoords[1] = coords[1]
                }
            }
            pi.next()
        }
        return minDistance
    }

    private fun getDistanceFromEdge(point: Point): Double {
        return getDistanceFromEdge(point.x, point.y)
    }

    private fun distanceToLineSegment(x: Double, y: Double, xStart: Double, yStart: Double, xEnd: Double, yEnd: Double): Double {
        val l2 = (xEnd - xStart).pow(2) + (yEnd - yStart).pow(2)
        if(l2 == 0.0) {
            return sqrt((x - xStart).pow(2) + (y - yStart).pow(2))
        }

        val t = max(0.0, min(1.0, ((x - xStart) * (xEnd - xStart) + (y - yStart) * (yEnd - yStart)) / l2))
        val xProj = xStart + t * (xEnd - xStart)
        val yProj = yStart + t * (yEnd - yStart)

        return sqrt((x - xProj).pow(2.0) + (y - yProj).pow(2.0))
    }
}