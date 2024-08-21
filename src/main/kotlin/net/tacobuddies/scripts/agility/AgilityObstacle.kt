package net.tacobuddies.scripts.agility

import net.runelite.api.coords.WorldPoint

data class AgilityObstacle(val objectId: Int, val action: String, val destination: WorldPoint)
