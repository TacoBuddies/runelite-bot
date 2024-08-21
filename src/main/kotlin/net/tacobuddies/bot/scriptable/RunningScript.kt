package net.tacobuddies.bot.scriptable

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*

private val logger = KotlinLogging.logger {}

class RunningScript(val script: Script) {
    private var running = false
    var paused = false
    private lateinit var job: Job

    private suspend fun run() = coroutineScope {
        job = launch {
            while (isActive && running) {
                try {


                    if (paused) {
                        delay(500)
                        continue
                    }

                    val sleep = script.loop()
                    if (sleep < 0) {
                        logger.warn { "Script tried sleeping for $sleep seconds. Terminating early" }
                        stop()
                        continue
                    }

                    delay(sleep.toLong())
                } catch (ignored: CancellationException) {
                } catch (e: Exception) {
                    logger.warn(e) { "The script cast an exception, continuing anyways" }
                    //stop()
                }
            }
        }

    }

    suspend fun start() {
        running = true
        this.run()
    }

    fun stop() {
        running = false
        script.onStop()
        job.cancel()
    }
}