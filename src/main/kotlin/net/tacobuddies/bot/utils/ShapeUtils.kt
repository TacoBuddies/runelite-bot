package net.tacobuddies.bot.utils

import net.runelite.api.Point
import java.awt.Shape
import java.awt.geom.PathIterator

object ShapeUtils {
    fun computeCenter(shape: Shape): Point {
        val iterator = shape.getPathIterator(null, 0.1)
        var coords = DoubleArray(6)
        var sumX = 0.0
        var sumY = 0.0
        var numPoints = 0

        while(!iterator.isDone) {
            val s = iterator.currentSegment(coords)
            when(s) {
                PathIterator.SEG_LINETO -> {
                    sumX += coords[0]
                    sumY += coords[1]
                    numPoints++
                }
            }
            iterator.next()
        }

        val x = sumX / numPoints
        val y = sumY / numPoints
        return Point(x.toInt(), y.toInt())
    }
}