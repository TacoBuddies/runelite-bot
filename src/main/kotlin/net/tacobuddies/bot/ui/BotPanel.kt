package net.tacobuddies.bot.ui

import net.runelite.client.eventbus.EventBus
import net.runelite.client.ui.ColorScheme
import net.runelite.client.ui.PluginPanel
import net.tacobuddies.bot.events.InputEnabled
import net.tacobuddies.bot.events.RandomsEnabled
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.inject.Inject
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class BotPanel : PluginPanel(false) {
    @Inject
    private lateinit var eventBus: EventBus

    private lateinit var blockInputPanel: BlockInputPanel
    private lateinit var enableRandomsPanel: EnableRandomsPanel
    private lateinit var scriptToolbarPanel: ScriptToolbarPanel
    private lateinit var  loadedScriptsPanel: LoadedScriptsPanel
    private lateinit var scriptDetailsPanel: ScriptDetailsPanel
    private lateinit var botToolbarPanel: BotToolbarPanel

    fun init() {
        blockInputPanel = BlockInputPanel(eventBus)
        enableRandomsPanel = EnableRandomsPanel(eventBus)
        scriptToolbarPanel = ScriptToolbarPanel(eventBus)
        loadedScriptsPanel = LoadedScriptsPanel(eventBus)
        scriptDetailsPanel = ScriptDetailsPanel()
        botToolbarPanel = BotToolbarPanel(eventBus)

        eventBus.register(blockInputPanel)
        eventBus.register(enableRandomsPanel)
        eventBus.register(scriptToolbarPanel)
        eventBus.register(loadedScriptsPanel)
        eventBus.register(scriptDetailsPanel)
        eventBus.register(botToolbarPanel)

        layout = BorderLayout()
        border = EmptyBorder(0, 10, 0, 10)
        background = ColorScheme.DARK_GRAY_COLOR

        val northPanel = JPanel().apply {
            border = EmptyBorder(10, 0, 10, 0)
            layout = GridLayout(0, 1, 0, 8)

            add(blockInputPanel)
            add(enableRandomsPanel)
            add(scriptToolbarPanel)
        }
        add(northPanel, BorderLayout.NORTH)

        val centerPanel = JPanel().apply {
            layout = BorderLayout()

            add(loadedScriptsPanel, BorderLayout.CENTER)
            add(scriptDetailsPanel, BorderLayout.SOUTH)
        }

        add(centerPanel, BorderLayout.CENTER)
        add(botToolbarPanel, BorderLayout.SOUTH)
    }

    fun dispose() {
        eventBus.post(InputEnabled(true))
        eventBus.post(RandomsEnabled(false))

        eventBus.unregister(blockInputPanel)
        eventBus.unregister(enableRandomsPanel)
        eventBus.unregister(scriptToolbarPanel)
        eventBus.unregister(loadedScriptsPanel)
        eventBus.unregister(scriptDetailsPanel)
        eventBus.unregister(botToolbarPanel)
    }
}