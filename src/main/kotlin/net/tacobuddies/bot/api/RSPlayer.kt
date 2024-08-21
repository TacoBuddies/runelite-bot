package net.tacobuddies.bot.api

import net.runelite.api.Player
import net.tacobuddies.bot.scriptable.ScriptContext

class RSPlayer(context: ScriptContext, val player: Player) : RSActor(context, player) {
    val name: String?
        get() = player.name

    val isMoving: Boolean
        get() {
            val poseAnimation = player.poseAnimation
            return poseAnimation == player.walkAnimation
                    || poseAnimation == player.runAnimation
                    || poseAnimation == player.walkRotateLeft
                    || poseAnimation == player.walkRotateRight
                    || poseAnimation == player.walkRotate180
        }

    val isLocalPlayerMoving: Boolean
        get() {
            val localDestination = context.client.localDestinationLocation ?: return false
            return context.client.localPlayer.localLocation == localDestination
        }

    val isIdle: Boolean
        get() = player.animation == -1

    val combatLevel: Int
        get() = player.combatLevel

}