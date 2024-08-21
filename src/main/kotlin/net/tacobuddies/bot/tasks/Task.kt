package net.tacobuddies.bot.tasks

import net.tacobuddies.bot.scriptable.TaskContext

interface Task {
    suspend fun run(context: TaskContext)
}