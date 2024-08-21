package net.tacobuddies.bot.ui

import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.ColorScheme
import net.tacobuddies.bot.events.ScriptSelected
import net.tacobuddies.bot.scriptable.ScriptDescriptor
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.TitledBorder

class ScriptDetailsPanel: JPanel() {
    private val name: JLabel = JLabel("")
    private val tags: JLabel = JLabel("")

    init {
        layout = GridBagLayout()
        border = TitledBorder(BorderFactory.createLineBorder(ColorScheme.LIGHT_GRAY_COLOR, 1, true), "Script Details")


        val constraints = GridBagConstraints().apply {
            weightx = 0.25
            ipadx = 4
            ipady = 4
            anchor = GridBagConstraints.LINE_START
            insets = Insets(0, 4, 0, 4)
        }
        add(JLabel("Name:"), constraints)

        constraints.gridy = 1
        add(JLabel("Tags:"), constraints)

        constraints.weightx = 1.75
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridy = 0
        constraints.gridx = 1
        add(name, constraints)

        constraints.gridy = 1
        add(tags, constraints)

        maximumSize = Dimension(maximumSize.width, 100)
    }

    @Subscribe
    fun onScriptSelected(event: ScriptSelected) {
        val script = event.script
        if(script == null) {
            name.text = ""
            tags.text = ""
            tags.toolTipText = null
        } else {
            val descriptor = script.javaClass.getAnnotation(ScriptDescriptor::class.java)
            name.text = descriptor.name

            if(descriptor.tags.isEmpty()) {
                tags.text = "None"
                tags.toolTipText = null
            } else {
                tags.text = descriptor.tags.joinToString(", ")
                tags.toolTipText = tags.text
            }
        }
    }
}