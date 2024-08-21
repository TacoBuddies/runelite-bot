package net.tacobuddies.bot.utils

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.spi.FilterReply
import org.slf4j.LoggerFactory
import org.slf4j.Marker

object Logging {
    fun filterNaturalMouseLogs() {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        context.addTurboFilter(object : TurboFilter() {
            override fun decide(p0: Marker?, p1: Logger?, p2: Level?, p3: String?, p4: Array<out Any>?, p5: Throwable?): FilterReply {
                return if(p1?.name?.contains("com.github.joonasvali", ignoreCase = true) == true) {
                    FilterReply.DENY
                } else {
                    FilterReply.NEUTRAL
                }
            }
        })
    }
}