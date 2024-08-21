package net.tacobuddies.bot.api

import net.runelite.api.Model
import net.runelite.api.Perspective
import net.runelite.api.Point
import net.runelite.api.coords.LocalPoint
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import net.tacobuddies.bot.utils.StdRandom
import java.awt.Polygon

abstract class RSModel(context: ScriptContext, protected val model: Model?) : ContextProvider<ScriptContext>(context) {

    protected open val localX: Int
        get() = -1

    protected open val localY: Int
        get() = -1

    protected open val localZ: Int
        get() = Perspective.getTileHeight(context.client, LocalPoint(localX, localY), context.client.plane)

    protected open val orientation: Int
        get() = 0

    val indexCount: Int
        get() = model?.faceCount ?: 0

    val verticesCount: Int
        get() = model?.verticesCount ?: 0

    protected open fun update() {}

    fun containsPoint(point: Point): Boolean {
        val triangles = getTriangles()
        for(triangle in triangles) {
            if(triangle.contains(point.x, point.y)) {
                return true
            }
        }
        return false
    }

    fun getPoint(): Point? {
        if(model == null) {
            return null
        }

        update()
        val len = model.verticesCount
        val sever = random(0, len)
        getPointInRange(sever, len)?.also { point ->
            return point
        }

        getPointInRange(0, sever)?.also { point ->
            return point
        }

        return Point(-1, -1)
    }

    fun getPoints(): List<Point> {
        val triangles = getTriangles()
        val points = mutableListOf<Point>()

        var index = 0
        for(triangle in triangles) {
            for(i in 0 ..< 3) {
                points.add(index++, Point(triangle.xpoints[i], triangle.ypoints[i]))
            }
        }

        return points.toList()
    }

    fun getPointOnScreen(): Point? {
        val list = mutableListOf<Point>()
        try {
            val triangles = getTriangles()
            for(poly in triangles) {
                for(j in 0 ..< poly.xpoints.size) {
                    val point = Point(poly.xpoints[j], poly.ypoints[j])
                    if(context.calculations.pointOnScreen(point)) {
                        return point
                    } else {
                        list.add(point)
                    }
                }
            }
        } catch(ignored: Exception) {}
        return if(list.size > 0) { list[random(0, list.size)] } else { null }
    }

    fun getCenterPoint(): Point {
        val triangles = getTriangles()
        var minX = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var minY = Int.MAX_VALUE
        var maxY = Int.MIN_VALUE

        for(triangle in triangles) {
            minX = minOf(minX, *triangle.xpoints)
            minY = minOf(minY, *triangle.ypoints)
            maxX = maxOf(maxX, *triangle.xpoints)
            maxY = maxOf(maxY, *triangle.ypoints)
        }

        return Point((maxX + minX) / 2, (maxY + minY) / 2)
    }

    fun getPointNearCenter(): Point {
        val triangles = getTriangles()
        var minX = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var minY = Int.MAX_VALUE
        var maxY = Int.MIN_VALUE

        for(triangle in triangles) {
            minX = minOf(minX, *triangle.xpoints)
            minY = minOf(minY, *triangle.ypoints)
            maxX = maxOf(maxX, *triangle.xpoints)
            maxY = maxOf(maxY, *triangle.ypoints)
        }

        val centerX = (maxX + minX) / 2
        val centerY = (maxY + minY) / 2

        val x = StdRandom.gaussian(minX.toDouble(), maxX.toDouble(), centerX.toDouble(), (maxX - minX) / 3.0)
        val y = StdRandom.gaussian(minY.toDouble(), maxY.toDouble(), centerY.toDouble(), (maxY - minY) / 3.0)

        return Point(x.toInt(), y.toInt())
    }

    fun getPointInRange(start: Int, end: Int): Point? {
        val localX = localX
        val localY = localY
        val height = context.calculations.tileHeight(localX, localY)

        val triangles = getTriangles()
        val points = mutableListOf<Point>()
        var i = start
        while (i < end && i < triangles.size) {
            for (n in 0 until triangles[i].npoints) {
                points.add(Point(triangles[i].xpoints[n], triangles[i].ypoints[n]))
            }
            i++
        }

        if(points.isEmpty()) {
            return null
        }

        return points[StdRandom.uniform(points.size)]
    }

    fun getTriangles(): List<Polygon> {
        if(model == null) {
            val tilePoly = Perspective.getCanvasTilePoly(context.client, LocalPoint(localX, localY))
            return listOf(tilePoly)
        }

        val count = model.verticesCount

        val x2d = IntArray(count)
        val y2d = IntArray(count)

        val localX = localX
        val localY = localY

        Perspective.modelToCanvas(context.client, count, localX, localY, localZ, orientation,
            model.verticesX, model.verticesZ, model.verticesY, x2d, y2d)
        val polys = mutableListOf<Polygon>()

        val trianglesX = model.faceIndices1
        val trianglesY = model.faceIndices2
        val trianglesZ = model.faceIndices3
        val avgTriLen = (trianglesX.size + trianglesY.size + trianglesZ.size) / 3.0

        for(triangle in 0 ..< count) {
            if(avgTriLen <= 1) {
                return emptyList()
            }

            if(avgTriLen <= triangle) {
                break
            }

            val xx = intArrayOf(
                x2d[trianglesX[triangle]],
                x2d[trianglesY[triangle]],
                x2d[trianglesZ[triangle]]
            )
            val yy = intArrayOf(
                y2d[trianglesX[triangle]],
                y2d[trianglesY[triangle]],
                y2d[trianglesZ[triangle]]
            )

            polys.add(Polygon(xx, yy, 3))
        }

        return polys.toList()
    }
}