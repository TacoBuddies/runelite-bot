package net.tacobuddies.bot.scriptable

import com.google.inject.Binder
import com.google.inject.Injector

abstract class RandomEvent : InstantiableModule {
    override lateinit var injector: Injector

    abstract fun shouldActivate(context: ScriptContext): Boolean
    abstract suspend fun run(context: ScriptContext)

    override fun configure(binder: Binder) {}
}