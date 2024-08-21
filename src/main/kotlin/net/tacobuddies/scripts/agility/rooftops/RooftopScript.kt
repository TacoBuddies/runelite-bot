package net.tacobuddies.scripts.agility.rooftops

import io.github.oshai.kotlinlogging.KotlinLogging
import net.runelite.api.Skill
import net.tacobuddies.bot.scriptable.Script
import net.tacobuddies.scripts.agility.AgilityObstacle

private val logger = KotlinLogging.logger {}
abstract class RooftopScript(private val obstacles: Array<AgilityObstacle>) : Script() {
    private var step: Int = 0

    abstract suspend fun onLapComplete()

    override fun onStop() {
        step = 0
    }

    override suspend fun loop(): Int {
        if(step == obstacles.size) {
            step = 0
            onLapComplete()
        } else if(step == -1) {
            step = obstacles.size - 1
        }

        val obstacle = obstacles[step]
        val `object` = objects.getNearestById(obstacle.objectId)
        if(`object` == null) {
            logger.warn { "Unable to find object ${obstacle.objectId}" }
            step--
            return 250
        }

        if(`object`.interactWith(obstacle.action)) {
            //Start moving towards obstacle
            if(!await(movementStart(1000))) {
                return 100
            }

            //We made it to the obstacle
            if(!await(inInteractionRange(`object`, 5000))) {
                return 100
            }

            //We completed the obstacle
            if(!awaitAll(skillChange(Skill.AGILITY, 10000), atLocation(obstacle.destination, 10000))) {
                return 100
            }

            step++
        }

        return 300
    }
}