package net.tacobuddies.bot.ui

import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe
import net.tacobuddies.bot.events.InputEnabled
import java.awt.BorderLayout
import java.awt.event.ItemEvent
import javax.swing.JCheckBox
import javax.swing.JPanel

class BlockInputPanel(private val eventBus: EventBus) : JPanel() {
    private var checkBox: JCheckBox

    init {
        layout = BorderLayout()

        checkBox = JCheckBox("Block Mouse Input")
        checkBox.isSelected = false
        checkBox.addItemListener {
            eventBus.post(InputEnabled(it.stateChange == ItemEvent.DESELECTED))
        }

        add(checkBox, BorderLayout.CENTER)
    }

    @Subscribe
    fun onInputEnabled(event: InputEnabled) {
        checkBox.isSelected = !event.enabled
    }
}