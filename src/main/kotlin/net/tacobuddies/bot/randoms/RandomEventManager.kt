package net.tacobuddies.bot.randoms

import com.google.inject.Module
import io.github.classgraph.ClassGraph
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import net.runelite.api.GameState
import net.runelite.client.RuneLite
import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe
import net.tacobuddies.bot.Agent
import net.tacobuddies.bot.BotState
import net.tacobuddies.bot.events.RandomsEnabled
import net.tacobuddies.bot.events.RequestBotState
import net.tacobuddies.bot.events.RequestScriptLoad
import net.tacobuddies.bot.scriptable.*
import java.io.File
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger {}
class RandomEventManager {
    private val loadedRandoms = mutableListOf<RandomEvent>()
    private val executor = Executors.newSingleThreadExecutor()
    private var randomsEnabled = true

    private lateinit var eventBus: EventBus
    private lateinit var context: ScriptContext
    private lateinit var job: Job

    fun init(eventBus: EventBus, context: ScriptContext) {
        this.eventBus = eventBus
        this.context = context

        executor.execute {
            runBlocking {
                loop()
            }
        }
    }

    private suspend fun loop() = coroutineScope {
        job = launch {
            while(isActive) {
                try {
                    if (randomsEnabled && context.client.gameState == GameState.LOGGED_IN) {
                        for (random in loadedRandoms) {
                            if (random.shouldActivate(context)) {
                                eventBus.post(RequestBotState(BotState.RANDOM))
                                random.run(context)
                                eventBus.post(RequestBotState(BotState.RESUME))
                            }
                        }
                    }

                    delay(600)
                } catch(ignored: CancellationException) {
                } catch(ignored: Exception) {}
            }
        }
    }

    private fun loadFromFile(file: File) {
        val classLoader = ScriptClassLoader(file.toURI().toURL())
        val graph = ClassGraph()
            .enableAllInfo()
            .overrideClasspath(file.absolutePath)
            .addClassLoader(classLoader)

        loadScripts(graph)
    }

    fun shutdown() {
        job.cancel()
    }

    fun loadInternal() {
        val agentPath =
            Agent::class.java.protectionDomain.codeSource.location.path

        loadFromFile(File(agentPath))
    }

    private fun loadScripts(graph: ClassGraph) {
        var count = 0
        graph.scan().use { result ->
            result.allClasses.loadClasses().forEach { clazz ->
                if (!RandomEvent::class.java.isAssignableFrom(clazz)) {
                    return@forEach
                }

                if(clazz == RandomEvent::class.java) {
                    return@forEach
                }

                try {
                    @Suppress("UNCHECKED_CAST")
                    val randomEvent = instantiate(clazz as Class<RandomEvent>)
                    if (randomEvent != null) {
                        loadedRandoms.add(randomEvent)
                        count++
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Error instantiating random event $clazz" }
                }
            }
        }

        logger.info { "Loaded $count random events" }
    }

    private fun <T : InstantiableModule> instantiate(clazz: Class<T>): T? {
        try {
            val script = clazz.getDeclaredConstructor().newInstance()

            val parentInjector = RuneLite.getInjector()
            val scriptModule = Module { binder ->
                binder.bind(clazz).toInstance(script)
                binder.install(script)
            }

            val scriptInjector = parentInjector.createChildInjector(scriptModule)
            script.injector = scriptInjector

            return script
        } catch (e: Exception) {
            logger.error(e) { "Error instantiating random event $clazz" }
            return null
        }
    }

    @Subscribe
    fun onRequestScriptLoad(event: RequestScriptLoad) {
        loadFromFile(event.file)
    }

    @Subscribe
    fun onRandomsEnabled(event: RandomsEnabled) {
        randomsEnabled = event.enabled
    }
}