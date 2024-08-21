package net.tacobuddies.bot.input

import net.runelite.api.Point

interface Mouse {
    suspend fun moveTo(x: Int, y: Int, dX: Int, dY: Int)
    suspend fun moveTo(point: Point, dX: Int, dY: Int)

    suspend fun moveTo(x: Int, y: Int)
    suspend fun moveTo(point: Point)

    suspend fun clickAt(x: Int, y: Int, rightClick: Boolean)
    suspend fun clickAt(point: Point, rightClick: Boolean)

    suspend fun dragTo(x: Int, y: Int)
    suspend fun dragTo(point: Point)
}