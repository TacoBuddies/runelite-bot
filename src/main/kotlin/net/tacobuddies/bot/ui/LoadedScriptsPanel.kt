package net.tacobuddies.bot.ui

import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.ColorScheme
import net.tacobuddies.bot.events.ScriptLoaded
import net.tacobuddies.bot.events.ScriptSelected
import net.tacobuddies.bot.events.ScriptsCleared
import net.tacobuddies.bot.scriptable.Script
import net.tacobuddies.bot.scriptable.ScriptDescriptor
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ListCellRenderer
import javax.swing.border.TitledBorder

class LoadedScriptsPanel(private val eventBus: EventBus) : JPanel() {
    private val model: DefaultListModel<Script> = DefaultListModel()
    private val list: JList<Script>

    init {
        layout = BorderLayout()
        border = TitledBorder(BorderFactory.createLineBorder(ColorScheme.LIGHT_GRAY_COLOR, 1, true), "Loaded Scripts")

        list = JList(model).apply {
            background = ColorScheme.DARKER_GRAY_COLOR
            selectionModel = ToggleableListSelectionModel()
            addListSelectionListener { eventBus.post(ScriptSelected(this.selectedValue)) }
            cellRenderer = object : JLabel(), ListCellRenderer<Script> {
                init { isOpaque = true }

                override fun getListCellRendererComponent(
                    list: JList<out Script>,
                    value: Script,
                    index: Int,
                    isSelected: Boolean,
                    cellHasFocus: Boolean
                ): Component {
                    val descriptor = value.javaClass.getAnnotation(ScriptDescriptor::class.java)
                    text = descriptor.name

                    if(isSelected) {
                        background = list.selectionBackground
                        foreground = list.selectionForeground
                    } else {
                        background = list.background
                        foreground = list.foreground
                    }

                    return this
                }
            }
        }

        add(JScrollPane(list), BorderLayout.CENTER)
    }

    @Subscribe
    fun onScriptLoaded(event: ScriptLoaded) {
        model.addElement(event.script)
    }

    @Subscribe
    fun onScriptsCleared(event: ScriptsCleared) {
        model.clear()
    }
}