package net.tacobuddies.bot.api

import kotlinx.coroutines.*
import net.runelite.api.TileObject
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.ScriptContext
import java.awt.Shape

class RSObject(context: ScriptContext, val `object`: TileObject, val type: Type) : ContextProvider<ScriptContext>(context), Interactable {
    private val hitbox = Hitbox(context, this, false)
    private val definition = context.objectManager.getObject(`object`.id)

    val name: String
        get() = definition?.name?.replace("<.*?>".toRegex(), "") ?: ""

    val actions: Array<String>
        get() = definition?.actions ?: emptyArray()

    val worldLocation: WorldPoint
        get() = `object`.worldLocation

    val area: WorldArea
        get() =  WorldArea(`object`.worldLocation, definition.sizeX, definition.sizeY)

    val location: RSTile?
        get() {
            val tile = context.calculations.getTile(`object`.worldLocation) ?: return null
            return RSTile(context, tile)
        }

    fun hasAction(action: String): Boolean {
        for(s in actions) {
            if(s.equals(action, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    suspend fun interactWith(action: String): Boolean {
        return interactWith(action, null, `object`.id)
    }

    override suspend fun interactWith(action: String, target: String?, identifier: Int?): Boolean {
        if(hitbox.interactWith(action, target, identifier)) {
            return true
        } else {
            context.camera.lookAt(`object`.worldLocation)
            return hitbox.interactWith(action, target, identifier)
        }
    }

    override suspend fun clickMouse(rightClick: Boolean): Boolean {
        if(hitbox.clickMouse(rightClick)) {
            return true
        }

        context.camera.lookAt(`object`.worldLocation)
        if(hitbox.clickMouse(rightClick)) {
            return true
        } else {
            val point = context.calculations.tileToScreen(`object`.worldLocation) ?: return false
            if(context.calculations.pointOnScreen(point)) {
                context.mouse.clickAt(point, rightClick)
                return true
            }
        }
        return false
    }

    override suspend fun hoverMouse(): Boolean {
        if(hitbox.hoverMouse()) {
            return true
        }

        context.camera.lookAt(`object`.worldLocation)
        if(hitbox.hoverMouse()) {
            return true
        } else {
            val point = context.calculations.tileToScreen(`object`.worldLocation) ?: return false
            if(context.calculations.pointOnScreen(point)) {
                context.mouse.moveTo(point)
                return true
            }
        }
        return false
    }

    override fun getHitboxShape(): Shape? {
        return `object`.clickbox
    }

    override fun getHitbox(): Hitbox {
        return hitbox
    }

    companion object {
        enum class Type(val bitValue: Int) {
            GAME(1), DECORATIVE(2), GROUND(4), WALL(8);

            companion object {
                fun getType(value: Int): Type? {
                    for (type in entries) {
                        if (type.bitValue == value) {
                            return type
                        }
                    }
                    return null
                }

                fun getMask(vararg types: Type?): Int {
                    var sum = 0
                    for (type in types) {
                        if (type != null) {
                            sum += type.bitValue
                        }
                    }
                    return sum
                }
            }
        }
    }
}