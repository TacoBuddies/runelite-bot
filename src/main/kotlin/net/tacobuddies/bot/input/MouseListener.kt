package net.tacobuddies.bot.input

import net.runelite.client.eventbus.Subscribe
import net.tacobuddies.bot.events.InputEnabled
import net.tacobuddies.bot.helpers.Mouse
import java.awt.event.MouseEvent

class MouseListener : net.runelite.client.input.MouseListener {
    private var inputEnabled: Boolean = true

    @Subscribe
    fun onInputEnabled(event: InputEnabled) {
        this.inputEnabled = event.enabled
    }
    
    override fun mouseClicked(event: MouseEvent): MouseEvent {
        if(!inputEnabled && !event.source.equals(Mouse.SOURCE)) {
            event.consume()
        }

        return event
    }

    override fun mousePressed(event: MouseEvent): MouseEvent {
        if(!inputEnabled && !event.source.equals(Mouse.SOURCE)) {
            event.consume()
        }

        return event
    }

    override fun mouseReleased(event: MouseEvent): MouseEvent {
        if(!inputEnabled && !event.source.equals(Mouse.SOURCE)) {
            event.consume()
        }

        return event
    }

    override fun mouseEntered(event: MouseEvent): MouseEvent {
        if(!inputEnabled && !event.source.equals(Mouse.SOURCE)) {
            event.consume()
        }

        return event
    }

    override fun mouseExited(event: MouseEvent): MouseEvent {
        if(!inputEnabled && !event.source.equals(Mouse.SOURCE)) {
            event.consume()
        }

        return event
    }

    override fun mouseDragged(event: MouseEvent): MouseEvent {
        if(!inputEnabled && !event.source.equals(Mouse.SOURCE)) {
            event.consume()
        }

        return event
    }

    override fun mouseMoved(event: MouseEvent): MouseEvent {
        if(!inputEnabled && !event.source.equals(Mouse.SOURCE)) {
            event.consume()
        }

        return event
    }
}