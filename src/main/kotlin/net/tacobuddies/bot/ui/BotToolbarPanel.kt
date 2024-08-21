package net.tacobuddies.bot.ui

import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe
import net.tacobuddies.bot.BotState
import net.tacobuddies.bot.events.BotStateChanged
import net.tacobuddies.bot.events.RequestBotState
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class BotToolbarPanel(private val eventBus: EventBus) : JPanel() {
    private val start = JButton("Start").apply {
        addActionListener { eventBus.post(RequestBotState(BotState.RUNNING)) }
    }

    private val pause = JButton("Pause").apply {
        isEnabled = false
        addActionListener { eventBus.post(RequestBotState(BotState.PAUSED)) }
    }

    private val stop = JButton("Stop").apply {
        isEnabled = false
        addActionListener { eventBus.post(RequestBotState(BotState.STOPPED)) }
    }

    init {
        layout = GridLayout(1, 0, 4, 0)
        border = EmptyBorder(10, 0, 10, 0)

        add(start)
        add(pause)
        add(stop)
    }

    @Subscribe
    fun onBotStateChanged(event: BotStateChanged) {
        when(event.state) {
            BotState.STOPPED -> {
                start.isEnabled = true
                pause.isEnabled = true
                stop.isEnabled = false
            }

            BotState.RUNNING -> {
                start.isEnabled = false
                pause.isEnabled = true
                stop.isEnabled = true
            }

            BotState.PAUSED -> {
                start.isEnabled = true
                pause.isEnabled = false
                stop.isEnabled = true
            }

            else -> {}
        }
    }
}