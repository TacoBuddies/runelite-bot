package net.tacobuddies.bot.helpers

import io.github.oshai.kotlinlogging.KotlinLogging
import net.runelite.api.Perspective
import net.runelite.api.Point
import net.runelite.api.Tile
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldPoint
import net.tacobuddies.bot.api.RSTile
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import java.awt.Polygon
import java.awt.Rectangle
import kotlin.math.abs

private val logger = KotlinLogging.logger {}
class Calculations(context: ScriptContext) : ContextProvider<ScriptContext>(context) {
    fun pointOnCanvas(point: Point): Boolean {
        val bounds = Rectangle(0, 0, context.client.canvasWidth, context.client.canvasHeight)
        return bounds.contains(point.x, point.y)
    }

    fun pointOnScreen(point: Point): Boolean {
        val bounds = Rectangle(context.client.viewportXOffset, context.client.viewportYOffset,
            context.client.viewportWidth, context.client.viewportHeight)
        return bounds.contains(point.x, point.y)
    }

    fun getTile(point: WorldPoint): Tile? {
        if(point.isInScene(context.client)) {
            val localPoint = LocalPoint.fromWorld(context.client, point)!!
            return context.client.scene.tiles[point.plane][localPoint.sceneX][localPoint.sceneY]
        }
        return null
    }

    fun getTile(point: LocalPoint): Tile? {
        if(point.isInScene) {
            return context.client.scene.tiles[context.client.plane][point.sceneX][point.sceneY]
        }
        return null
    }

    fun tileOnScreen(tile: Tile): Boolean {
        val point = tileToScreen(tile) ?: return false
        return pointOnScreen(point)
    }

    fun tileToScreen(point: LocalPoint): Point? {
        if(point.isInScene) {
            val tile = context.client.scene.tiles[context.client.plane][point.sceneX][point.sceneY]
            return tileToScreen(tile)
        }

        return null
    }

    fun tileToScreen(point: WorldPoint): Point? {
        if(point.isInScene(context.client)) {
            val localPoint = LocalPoint.fromWorld(context.client, point)!!
            val tile = context.client.scene.tiles[point.plane][localPoint.sceneX][localPoint.sceneY]
            return tileToScreen(tile)
        }

        return null
    }

    fun tileToScreen(tile: Tile): Point? {
        return Perspective.localToCanvas(context.client, tile.localLocation, context.client.plane, 0)
    }

    fun tileHeight(x: Int, y: Int): Int {
        return Perspective.getTileHeight(context.client, LocalPoint(x, y), context.client.plane)
    }

    fun tileOnMap(tile: RSTile?): Boolean {
        return tileToMinimap(tile) != null
    }

    fun tileToMinimap(tile: RSTile?): Point? {
        if(tile == null) return null
        return worldToMinimap(tile.worldLocation.x, tile.worldLocation.y)
    }

    fun worldToMinimap(x: Int, y: Int): Point? {
        val test = LocalPoint.fromWorld(context.client, x, y)
        if(test != null)
            return Perspective.localToMinimap(context.client, test, 2500)
        return null
    }

    fun canReach(dest: RSTile?, isObject: Boolean): Boolean {
        return pathLengthTo(dest, isObject) != -1
    }

    fun pathLengthTo(dest: RSTile?, isObject: Boolean): Int {
        if(dest == null) return -1
        val curPos = context.client.localPlayer.worldLocation
        return pathLengthBetween(curPos, dest, isObject)
    }

    fun pathLengthBetween(start: WorldPoint, dest: RSTile, isObject: Boolean): Int {
        return dijkstraDist(
            start.x - context.client.baseX,  // startX
            start.y - context.client.baseY,  // startY
            dest.worldLocation.x - context.client.baseX,  // destX
            dest.worldLocation.y - context.client.baseY,  // destY
            isObject
        ) // if it's an object, accept any adjacent tile
    }

    private fun dijkstraDist(
        startX: Int, startY: Int, destX: Int, destY: Int,
        isObject: Boolean
    ): Int {
        val prev = Array(104) { IntArray(104) }
        val dist = Array(104) { IntArray(104) }
        val path_x = IntArray(4000)
        val path_y = IntArray(4000)
        for (xx in 0..103) {
            for (yy in 0..103) {
                prev[xx][yy] = 0
                dist[xx][yy] = 99999999
            }
        }
        var curr_x = startX
        var curr_y = startY
        prev[startX][startY] = 99
        dist[startX][startY] = 0
        var path_ptr = 0
        var step_ptr = 0
        path_x[path_ptr] = startX
        path_y[path_ptr++] = startY
        val blocks: Array<ByteArray> = context.client.tileSettings[context.client.plane]
        val pathLength = path_x.size
        var foundPath = false
        while (step_ptr != path_ptr) {
            curr_x = path_x[step_ptr]
            curr_y = path_y[step_ptr]
            if (abs((curr_x - destX)) + abs((curr_y - destY)) == (if (isObject) 1 else 0)) {
                foundPath = true
                break
            }
            step_ptr = (step_ptr + 1) % pathLength
            val cost = dist[curr_x][curr_y] + 1
            // south
            if ((curr_y > 0) && (prev[curr_x][curr_y - 1] == 0) && ((blocks[curr_x + 1][curr_y].toInt() and 0x1280102) == 0)) {
                path_x[path_ptr] = curr_x
                path_y[path_ptr] = curr_y - 1
                path_ptr = (path_ptr + 1) % pathLength
                prev[curr_x][curr_y - 1] = 1
                dist[curr_x][curr_y - 1] = cost
            }
            // west
            if ((curr_x > 0) && (prev[curr_x - 1][curr_y] == 0) && ((blocks[curr_x][curr_y + 1].toInt() and 0x1280108) == 0)) {
                path_x[path_ptr] = curr_x - 1
                path_y[path_ptr] = curr_y
                path_ptr = (path_ptr + 1) % pathLength
                prev[curr_x - 1][curr_y] = 2
                dist[curr_x - 1][curr_y] = cost
            }
            // north
            if ((curr_y < 104 - 1) && (prev[curr_x][curr_y + 1] == 0) && ((blocks[curr_x + 1][curr_y + 2].toInt() and
                        0x1280120) == 0)
            ) {
                path_x[path_ptr] = curr_x
                path_y[path_ptr] = curr_y + 1
                path_ptr = (path_ptr + 1) % pathLength
                prev[curr_x][curr_y + 1] = 4
                dist[curr_x][curr_y + 1] = cost
            }
            // east
            if ((curr_x < 104 - 1) && (prev[curr_x + 1][curr_y] == 0) && ((blocks[curr_x + 2][curr_y + 1].toInt() and
                        0x1280180) == 0)
            ) {
                path_x[path_ptr] = curr_x + 1
                path_y[path_ptr] = curr_y
                path_ptr = (path_ptr + 1) % pathLength
                prev[curr_x + 1][curr_y] = 8
                dist[curr_x + 1][curr_y] = cost
            }
            // south west
            if ((curr_x > 0) && (curr_y > 0) && (prev[curr_x - 1][curr_y - 1] == 0) && ((blocks[curr_x][curr_y].toInt() and
                        0x128010e) == 0) && ((blocks[curr_x][curr_y + 1].toInt() and 0x1280108) == 0) && ((blocks[curr_x +
                        1][curr_y].toInt() and 0x1280102) == 0)
            ) {
                path_x[path_ptr] = curr_x - 1
                path_y[path_ptr] = curr_y - 1
                path_ptr = (path_ptr + 1) % pathLength
                prev[curr_x - 1][curr_y - 1] = 3
                dist[curr_x - 1][curr_y - 1] = cost
            }
            // north west
            if ((curr_x > 0) && (curr_y < 104 - 1) && (prev[curr_x - 1][curr_y + 1] == 0) && ((blocks[curr_x][curr_y + 2].toInt() and 0x1280138) == 0) && ((blocks[curr_x][curr_y + 1].toInt() and 0x1280108) ==
                        0) && ((blocks[curr_x + 1][curr_y + 2].toInt() and 0x1280120) == 0)
            ) {
                path_x[path_ptr] = curr_x - 1
                path_y[path_ptr] = curr_y + 1
                path_ptr = (path_ptr + 1) % pathLength
                prev[curr_x - 1][curr_y + 1] = 6
                dist[curr_x - 1][curr_y + 1] = cost
            }
            // south east
            if ((curr_x < 104 - 1) && (curr_y > 0) && (prev[curr_x + 1][curr_y - 1] == 0) && ((blocks[curr_x +
                        2][curr_y].toInt() and 0x1280183) == 0) && ((blocks[curr_x + 2][curr_y + 1].toInt() and 0x1280180) == 0) && ((blocks[curr_x + 1][curr_y].toInt() and 0x1280102) == 0)
            ) {
                path_x[path_ptr] = curr_x + 1
                path_y[path_ptr] = curr_y - 1
                path_ptr = (path_ptr + 1) % pathLength
                prev[curr_x + 1][curr_y - 1] = 9
                dist[curr_x + 1][curr_y - 1] = cost
            }
            // north east
            if ((curr_x < 104 - 1) && (curr_y < 104 - 1) && (prev[curr_x + 1][curr_y + 1] == 0) && ((blocks[curr_x
                        + 2][curr_y + 2].toInt() and 0x12801e0) == 0) && ((blocks[curr_x + 2][curr_y + 1].toInt() and 0x1280180) == 0) && ((blocks[curr_x + 1][curr_y + 2].toInt() and 0x1280120) == 0)
            ) {
                path_x[path_ptr] = curr_x + 1
                path_y[path_ptr] = curr_y + 1
                path_ptr = (path_ptr + 1) % pathLength
                prev[curr_x + 1][curr_y + 1] = 12
                dist[curr_x + 1][curr_y + 1] = cost
            }
        }
        return if (foundPath) dist[curr_x][curr_y] else -1
    }

    fun getTileBoundsPoly(tile: Tile): Polygon {
        return Perspective.getCanvasTilePoly(context.client, tile.localLocation)
    }
}