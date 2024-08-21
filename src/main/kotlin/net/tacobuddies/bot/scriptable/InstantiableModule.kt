package net.tacobuddies.bot.scriptable

import com.google.inject.Injector
import com.google.inject.Module

interface InstantiableModule : Module {
    var injector: Injector
}