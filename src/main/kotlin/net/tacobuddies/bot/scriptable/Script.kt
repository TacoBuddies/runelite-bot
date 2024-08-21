package net.tacobuddies.bot.scriptable

import com.google.inject.Binder
import com.google.inject.Injector
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.runelite.api.Client
import net.runelite.api.Skill
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.StatChanged
import net.runelite.client.eventbus.Subscribe
import net.tacobuddies.bot.api.RSObject
import net.tacobuddies.bot.api.RSPlayer
import net.tacobuddies.bot.helpers.*

abstract class Script : InstantiableModule {
    override lateinit var injector: Injector
    lateinit var context: ScriptContext

    private val statChangedEvent = MutableSharedFlow<StatChanged>(0, 100)

    val client: Client by lazy { context.client }
    val game: Game by lazy { context.game }
    val gameUI: GameUI by lazy { context.gameUI }
    val mouse: Mouse by lazy { context.mouse }
    val camera: Camera by lazy { context.camera }
    val keyboard: Keyboard by lazy { context.keyboard }
    val menu: Menu by lazy { context.menu }
    val interfaces: Interfaces by lazy { context.interfaces }
    val players: Players by lazy { context.players }
    val npcs: Npcs by lazy { context.npcs }
    val objects: Objects by lazy { context.objects }
    val inventory: Inventory by lazy { context.inventory }
    val equipment: Equipment by lazy { context.equipment }
    val groundItems: GroundItems by lazy { context.groundItems }
    val localPlayer: RSPlayer by lazy { players.localPlayer }

    abstract fun onStart(): Boolean
    abstract fun onStop()
    abstract suspend fun loop(): Int

    override fun configure(binder: Binder) {}

    suspend fun await(job: Job): Boolean {
        try {
            job.join()
            return true
        } catch(ignored: TimeoutCancellationException) {
            return false
        }
    }

    suspend fun awaitAll(vararg jobs: Job): Boolean {
        try {
            joinAll(*jobs)
            return true
        } catch(ignored: TimeoutCancellationException) {
            return false
        }
    }

    @Throws(TimeoutCancellationException::class)
    suspend fun atLocation(point: WorldPoint, timeout: Long = -1L): Job {
        return launchJob(timeout) {
            while (isActive && client.localPlayer.worldLocation.distanceTo(point) > 0) {
                delay(100)
            }
        }
    }

    @Throws(TimeoutCancellationException::class)
    suspend fun leaveLocation(distanceThreshold: Int, timeout: Long = -1L): Job {
        return launchJob(timeout) {
            val start = client.localPlayer.worldLocation
            while(isActive && client.localPlayer.worldLocation.distanceTo(start) < distanceThreshold) {
                delay(100)
            }
        }
    }

    @Throws(TimeoutCancellationException::class)
    suspend fun movementStart(timeout: Long = -1L): Job {
        return launchJob(timeout) {
            while(isActive && !(localPlayer.isMoving || localPlayer.isLocalPlayerMoving)) {
                delay(100)
            }
        }
    }

    @Throws(TimeoutCancellationException::class)
    suspend fun movementStop(timeout: Long = -1L): Job {
        return launchJob(timeout) {
            while(isActive && (localPlayer.isMoving || localPlayer.isLocalPlayerMoving)) {
                delay(100)
            }
        }
    }

    @Throws(TimeoutCancellationException::class)
    suspend fun inInteractionRange(obj: RSObject, timeout: Long = -1L): Job {
        return launchJob(timeout) {
            while(isActive && (localPlayer.worldLocation.distanceTo(obj.area) > 1)) {
                delay(100)
            }
        }
    }

    @Throws(TimeoutCancellationException::class)
    suspend fun outOfInteractionRange(obj: RSObject, timeout: Long = -1L): Job {
        return launchJob(timeout) {
            while(isActive && (localPlayer.worldLocation.distanceTo(obj.area) <= 1)) {
                delay(100)
            }
        }
    }

    @Throws(TimeoutCancellationException::class)
    suspend fun interactionStart(timeout: Long = -1L): Job {
        return launchJob(timeout) {
            while (isActive && client.localPlayer.interacting == null) {
                delay(100)
            }
        }
    }

    @Throws(TimeoutCancellationException::class)
    suspend fun interactionStop(timeout: Long = -1L): Job {
        return launchJob(timeout) {
            while(isActive && client.localPlayer.interacting != null) {
                delay(100)
            }
        }
    }

    @Throws(TimeoutCancellationException::class)
    suspend fun animationStart(timeout: Long = -1L): Job {
        return launchJob(timeout) {
            while(isActive && client.localPlayer.animation == -1) {
                delay(100)
            }
        }
    }

    @Throws(TimeoutCancellationException::class)
    suspend fun animationStop(timeout: Long = -1L): Job {
        return launchJob(timeout) {
            while(isActive && client.localPlayer.animation != -1) {
                delay(100)
            }
        }
    }

    @Throws(TimeoutCancellationException::class)
    suspend fun skillChange(skill: Skill, timeout: Long = -1L): Job {
        return launchJob(timeout) {
            statChangedEvent.collect {
                if(!isActive) {
                    return@collect
                }

                if (it.skill == skill) {
                    this.coroutineContext.job.cancel()
                }
            }
        }
    }

    @Throws(TimeoutCancellationException::class)
    suspend fun waitFor(timeout: Long = -1L, predicate: (ScriptContext) -> Boolean): Job {
        return launchJob(timeout) {
            while (isActive && !predicate.invoke(context)) {
                delay(100)
            }
        }
    }

    @Throws(TimeoutCancellationException::class)
    private suspend fun launchJob(
        timeout: Long = -1L,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return coroutineScope {
            launch {
                val timeMillis = if(timeout > 0) { timeout } else { Long.MAX_VALUE }
                withTimeout(timeMillis) {
                    block()
                }
            }
        }
    }

    @Subscribe
    fun onStatChanged(event: StatChanged) {
        statChangedEvent.tryEmit(event)
    }
}