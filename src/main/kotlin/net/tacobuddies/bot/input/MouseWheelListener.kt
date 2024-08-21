package net.tacobuddies.bot.input

import net.runelite.client.eventbus.Subscribe
import net.tacobuddies.bot.events.InputEnabled
import net.tacobuddies.bot.helpers.Mouse
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent

class MouseWheelListener : net.runelite.client.input.MouseWheelListener {
    private var inputEnabled: Boolean = true

    @Subscribe
    fun onInputEnabled(event: InputEnabled) {
        this.inputEnabled = event.enabled
    }

    override fun mouseWheelMoved(event: MouseWheelEvent): MouseWheelEvent {
        if(!inputEnabled && !event.source.equals(Mouse.SOURCE)) {
            event.consume()
        }

        return event
    }
}