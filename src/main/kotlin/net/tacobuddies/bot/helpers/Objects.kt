package net.tacobuddies.bot.helpers

import net.runelite.api.coords.WorldPoint
import net.tacobuddies.bot.api.RSObject
import net.tacobuddies.bot.api.RSTile
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import java.util.function.Predicate

class Objects(context: ScriptContext) : ContextProvider<ScriptContext>(context) {
    private val client = context.client

    fun getAll(): List<RSObject> {
        return getAll { true }
    }

    fun getAll(predicate: Predicate<RSObject>): List<RSObject> {
        val objects = linkedSetOf<RSObject>()
        for(x in 0 ..< 104) {
            for(y in 0 ..< 104) {
                for(obj in getAtLocal(x, y, -1)) {
                    if(predicate.test(obj)) {
                        objects.add(obj)
                    }
                }
            }
        }

        return objects.toList()
    }

    fun getNearest(distanceCutoff: Int, predicate: Predicate<RSObject>): RSObject? {
        var current: RSObject? = null
        var distance = -1
        for(x in 0 ..< 104) {
            for(y in 0 ..< 104) {
                val distanceToCheck = client.localPlayer.worldLocation.distanceTo(WorldPoint(x, y, client.plane))
                if(distanceToCheck < distanceCutoff) {
                    val objects = getAtLocal(x, y, -1)
                    for(obj in objects) {
                        if(predicate.test(obj)) {
                            val tmp = client.localPlayer.worldLocation.distanceTo(obj.`object`.worldLocation)
                            if(current == null) {
                                distance = tmp
                                current = obj
                            } else if(tmp < distance) {
                                current = obj
                                distance = tmp
                            }
                            break
                        }
                    }
                }
            }
        }
        return current
    }

    fun getNearest(predicate: Predicate<RSObject>): RSObject? {
        var current: RSObject? = null
        var distance = Int.MAX_VALUE
        for(x in 0 ..< 104) {
            for(y in 0 ..< 104) {
                val objects = getAtLocal(x, y, -1)
                for(obj in objects) {
                    if(predicate.test(obj)) {
                        val tmp = client.localPlayer.worldLocation.distanceTo(obj.`object`.worldLocation)
                        if(tmp < distance) {
                            current = obj
                            distance = tmp
                        }
                    }
                }
            }
        }
        return current
    }

    fun getNearestById(vararg ids: Int): RSObject? {
        return getNearest { obj ->
            for(id in ids) {
                if(obj.`object`.id == id) {
                    return@getNearest true
                }
            }
            return@getNearest false
        }
    }

    fun getTopAt(tile: RSTile): RSObject? {
        return getTopAt(tile, -1)
    }

    fun getTopAt(tile: RSTile, mask: Int): RSObject? {
        val objects = getAt(tile, mask)
        return if(objects.isNotEmpty()) { objects[0] } else { null }
    }

    fun getAllAt(tile: RSTile): List<RSObject> {
        return getAtLocal(tile.tile.localLocation.sceneX, tile.tile.localLocation.sceneY, -1).toList()
    }

    fun getAt(tile: RSTile, mask: Int): List<RSObject> {
        return getAtLocal(tile.tile.localLocation.sceneX, tile.tile.localLocation.sceneY, mask).toList()
    }

    private fun getAtLocal(x: Int, y: Int, mask: Int): Set<RSObject> {
        val objects = linkedSetOf<RSObject>()
        if(client.tileSettings == null) {
            return objects
        }

        val plane = client.plane
        val tile = client.scene.tiles[plane][x][y]

        if(tile != null) {
            if(mask == -1 || (mask and 1) == 1) {
                for(gameObject in tile.gameObjects) {
                    if(gameObject != null) {
                        objects.add(RSObject(context, gameObject, RSObject.Companion.Type.GAME))
                    }
                }
            }

            if(mask == -1 || (mask shr 1 and 1) == 1) {
                val tileObject = tile.decorativeObject
                if(tileObject != null) {
                    objects.add(RSObject(context, tileObject, RSObject.Companion.Type.DECORATIVE))
                }
            }

            if(mask == -1 || (mask shr 2 and 1) == 1) {
                val groundObject = tile.groundObject
                if(groundObject != null) {
                    objects.add(RSObject(context, groundObject, RSObject.Companion.Type.GROUND))
                }
            }

            if(mask == -1 || (mask shr 3 and 1) == 1) {
                val wallObject = tile.wallObject
                if(wallObject != null) {
                    objects.add(RSObject(context, wallObject, RSObject.Companion.Type.WALL))
                }
            }
        }

        return objects.toSet()
    }
}