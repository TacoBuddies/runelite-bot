package net.tacobuddies.bot.scriptable

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ScriptDescriptor(val name: String, val enabled: Boolean, val tags: Array<String> = [])
