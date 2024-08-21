package net.tacobuddies.bot.ui

import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe
import net.tacobuddies.bot.events.RandomsEnabled
import java.awt.BorderLayout
import java.awt.event.ItemEvent
import javax.swing.JCheckBox
import javax.swing.JPanel

class EnableRandomsPanel(private val eventBus: EventBus) : JPanel() {
    private var checkBox: JCheckBox

    init {
        layout = BorderLayout()

        checkBox = JCheckBox("Enable Random Events")
        checkBox.isSelected = true
        checkBox.addItemListener {
            eventBus.post(RandomsEnabled(it.stateChange == ItemEvent.SELECTED))
        }

        add(checkBox, BorderLayout.CENTER)
    }

    @Subscribe
    fun onRandomsEnabled(event: RandomsEnabled) {
        checkBox.isSelected = event.enabled
    }
}