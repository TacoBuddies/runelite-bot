package net.tacobuddies.bot.api

import java.awt.Shape

interface Interactable {
    suspend fun interactWith(action: String, target: String?, identifier: Int?): Boolean
    suspend fun clickMouse(rightClick: Boolean): Boolean
    suspend fun hoverMouse(): Boolean

    fun getHitboxShape(): Shape?
    fun getHitbox(): Hitbox
}