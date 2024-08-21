package net.tacobuddies.bot.ui

import net.runelite.client.eventbus.EventBus
import net.tacobuddies.bot.events.RequestScriptLoad
import net.tacobuddies.bot.events.ScriptsCleared
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JPanel
import javax.swing.filechooser.FileNameExtensionFilter

class ScriptToolbarPanel(private val eventBus: EventBus) : JPanel() {

    init {
        layout = GridLayout(1, 0, 4, 0)

        val load = JButton("Load Scripts")
        load.addActionListener { showLoadDialog() }

        val clear = JButton("Clear Scripts")
        clear.addActionListener { eventBus.post(ScriptsCleared()) }

        add(load)
        add(clear)
    }

    private fun showLoadDialog() {
        val fileChooser = JFileChooser().apply {
            fileFilter = FileNameExtensionFilter(".JAR files", "jar")
        }

        val selection = fileChooser.showOpenDialog(this)
        if(selection == JFileChooser.APPROVE_OPTION) {
            eventBus.post(RequestScriptLoad(fileChooser.selectedFile))
        }
    }
}