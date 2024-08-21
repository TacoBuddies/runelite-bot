package net.tacobuddies.bot.scriptable

import com.google.inject.Module
import io.github.classgraph.ClassGraph
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.runelite.api.Client
import net.runelite.client.RuneLite
import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe
import net.tacobuddies.bot.Agent
import net.tacobuddies.bot.events.RequestScriptLoad
import net.tacobuddies.bot.events.ScriptLoaded
import net.tacobuddies.bot.events.ScriptSelected
import net.tacobuddies.bot.events.ScriptsCleared
import net.tacobuddies.bot.tasks.Task
import java.io.File
import java.lang.reflect.Modifier
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger {}

class ScriptManager {
    private lateinit var eventBus: EventBus
    private lateinit var context: ScriptContext

    private val loadedScripts = mutableListOf<Script>()
    private val executor = Executors.newSingleThreadExecutor()

    private var selectedScript: Script? = null
    private var runningScript: RunningScript? = null

    fun init(eventBus: EventBus, context: ScriptContext) {
        this.eventBus = eventBus
        this.context = context
    }

    private fun loadFromFile(file: File) {
        val classLoader = ScriptClassLoader(file.toURI().toURL())
        val graph = ClassGraph()
            .enableAllInfo()
            .overrideClasspath(file.absolutePath)
            .addClassLoader(classLoader)

        loadScripts(graph)
    }

    fun loadInternal() {
        val agentPath =
            Agent::class.java.protectionDomain.codeSource.location.path

        loadFromFile(File(agentPath))
    }

    fun startScript(): Boolean {
        if (selectedScript == null) return false

        if (runningScript != null) {
            runningScript?.paused = false
            return true
        }

        val script = selectedScript!!
        script.context = context

        if (script.onStart()) {
            eventBus.register(script)
            eventBus.register(Script::class.java)
            executor.execute {
                runBlocking {
                    val rs = RunningScript(script)
                    runningScript = rs
                    rs.start()
                }
            }
            return true
        }

        return false
    }

    fun stopScript() {
        if (runningScript != null) {
            runningScript!!.stop()
            eventBus.unregister(runningScript!!.script)
            runningScript = null
        }
    }

    fun pauseScript() {
        if (runningScript != null) {
            runningScript!!.paused = true
        }
    }

    fun resumeScript() {
        if(runningScript != null) {
            runningScript!!.paused = false
        }
    }

    fun runTask(task: Task, client: Client) {
        executor.execute {
            runBlocking {
                task.run(TaskContext(client))
            }
        }
    }

    private fun loadScripts(graph: ClassGraph) {
        var count = 0

        graph.scan().use { result ->
            result.allClasses.loadClasses().forEach { clazz ->
                val descriptor = clazz.getAnnotation(ScriptDescriptor::class.java)
                if (descriptor == null) {
                    if (clazz.superclass == Script::class.java && !Modifier.isAbstract(clazz.modifiers)) {
                        logger.error { "Script $clazz is missing a @ScriptDescriptor" }
                    }
                    return@forEach
                }

                if (!Script::class.java.isAssignableFrom(clazz)) {
                    logger.error { "Class $clazz has a @ScriptDescriptor but does not extend Script" }
                    return@forEach
                }

                if (descriptor.enabled) {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val script = instantiate(clazz as Class<Script>)
                        if (script != null) {
                            loadedScripts.add(script)
                            eventBus.post(ScriptLoaded(script))
                            count++
                        }
                    } catch (e: Exception) {
                        logger.error(e) { "Error instantiating script $clazz" }
                    }
                }
            }
        }

        logger.info { "Loaded $count scripts" }
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
            logger.error(e) { "Error instantiating script $clazz" }
            return null
        }
    }

    @Subscribe
    fun onRequestScriptLoad(event: RequestScriptLoad) {
        loadFromFile(event.file)
    }

    @Subscribe
    fun onScriptsCleared(event: ScriptsCleared) {
        loadedScripts.clear()
    }

    @Subscribe
    fun onScriptSelected(event: ScriptSelected) {
        selectedScript = event.script
    }
}