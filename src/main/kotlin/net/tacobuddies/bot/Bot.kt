package net.tacobuddies.bot

import com.google.inject.Provides
import io.github.oshai.kotlinlogging.KotlinLogging
import net.runelite.api.*
import net.runelite.api.events.*
import net.runelite.cache.ItemManager
import net.runelite.cache.NpcManager
import net.runelite.cache.ObjectManager
import net.runelite.cache.fs.Store
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.EventBus
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.input.MouseManager
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.ui.ClientToolbar
import net.runelite.client.ui.NavigationButton
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.util.ImageUtil
import net.tacobuddies.bot.account.AccountManager
import net.tacobuddies.bot.events.BotStateChanged
import net.tacobuddies.bot.events.InputEnabled
import net.tacobuddies.bot.events.RequestBotState
import net.tacobuddies.bot.input.MouseListener
import net.tacobuddies.bot.input.MouseWheelListener
import net.tacobuddies.bot.overlay.CrosshairOverlay
import net.tacobuddies.bot.randoms.RandomEventManager
import net.tacobuddies.bot.scriptable.ScriptContext
import net.tacobuddies.bot.scriptable.ScriptManager
import net.tacobuddies.bot.tasks.LoginTask
import net.tacobuddies.bot.ui.BotPanel
import net.tacobuddies.bot.utils.Logging
import java.io.File
import javax.inject.Inject

private val logger = KotlinLogging.logger {}

@PluginDescriptor(
    name = "TacoBuddies Bot",
    configName = "tacobuddies"
)
class Bot : Plugin() {
    @Inject
    private lateinit var config: BotConfig

    @Inject
    private lateinit var botPanel: BotPanel

    @Inject
    private lateinit var crosshairOverlay: CrosshairOverlay

    @Inject
    lateinit var client: Client

    @Inject
    private lateinit var clientThread: ClientThread

    @Inject
    private lateinit var clientToolbar: ClientToolbar

    @Inject
    private lateinit var overlayManager: OverlayManager

    @Inject
    private lateinit var mouseManager: MouseManager

    @Inject
    private lateinit var eventBus: EventBus

    private val mouseListener = MouseListener()
    private val mouseWheelListener = MouseWheelListener()
    private val scriptManager = ScriptManager()
    private val randomEventManager = RandomEventManager()
    private val accountManager = AccountManager()
    private var state = BotState.STOPPED
    private var firstLogin = true

    private lateinit var navButton: NavigationButton
    private lateinit var store: Store
    private lateinit var objectManager: ObjectManager
    private lateinit var npcManager: NpcManager
    private lateinit var itemManager: ItemManager
    private lateinit var context: ScriptContext

    override fun startUp() {
        Logging.filterNaturalMouseLogs()
        store = Store(File(RuneLite.RUNELITE_DIR, System.getProperty("net.tacobuddies.cache", "jagexcache/oldschool/LIVE")))
        objectManager = ObjectManager(store)
        npcManager = NpcManager(store)
        itemManager = ItemManager(store)
        context = ScriptContext(client, objectManager, npcManager, itemManager, accountManager)

        scriptManager.init(eventBus, context)
        randomEventManager.init(eventBus, context)
        overlayManager.add(crosshairOverlay)

        botPanel.init()
        val icon = ImageUtil.loadImageResource(Bot::class.java, "bot_icon.png")
        navButton = NavigationButton.builder()
            .tooltip("TacoBuddies Bot")
            .icon(icon)
            .panel(botPanel)
            .priority(10)
            .build()

        clientToolbar.addNavigation(navButton)
        mouseManager.registerMouseListener(0, mouseListener)
        mouseManager.registerMouseWheelListener(0, mouseWheelListener)

        eventBus.register(mouseListener)
        eventBus.register(mouseWheelListener)
        eventBus.register(scriptManager)
        eventBus.register(randomEventManager)

        clientThread.invokeLater(Runnable {
            store.load()
            objectManager.load()
            npcManager.load()
            itemManager.load()
        })
        clientThread.invokeLater(Runnable { scriptManager.loadInternal() })
        clientThread.invokeLater(Runnable { randomEventManager.loadInternal() })
        clientThread.invokeLater(Runnable { accountManager.load(File("./accounts.json")) })
    }

    override fun shutDown() {
        overlayManager.remove(crosshairOverlay)

        randomEventManager.shutdown()
        scriptManager.stopScript()

        botPanel.dispose()
        clientToolbar.removeNavigation(navButton)
        mouseManager.unregisterMouseListener(mouseListener)
        mouseManager.unregisterMouseWheelListener(mouseWheelListener)

        eventBus.unregister(mouseListener)
        eventBus.unregister(mouseWheelListener)
        eventBus.unregister(scriptManager)
        eventBus.unregister(randomEventManager)
    }

    @Provides
    fun provideConfig(configManager: ConfigManager): BotConfig {
        return configManager.getConfig(BotConfig::class.java)
    }

    @Subscribe
    fun onRequestBotState(event: RequestBotState) {
        if (state == event.state)
            return

        when (event.state) {
            BotState.STOPPED -> {
                scriptManager.stopScript()
                setState(BotState.STOPPED)
                eventBus.post(InputEnabled(true))
            }

            BotState.RUNNING -> {
                if (scriptManager.startScript()) {
                    eventBus.post(InputEnabled(false))
                    setState(BotState.RUNNING)
                }
            }

            BotState.PAUSED -> {
                scriptManager.pauseScript()
                setState(BotState.PAUSED)
                eventBus.post(InputEnabled(true))
            }

            BotState.RANDOM -> {
                scriptManager.pauseScript()
            }

            BotState.RESUME -> {
                scriptManager.resumeScript()
            }
        }
    }

    @Subscribe
    fun onMenuEntryAdded(event: MenuEntryAdded) {
        val entry = event.menuEntry
        val action = entry.type
        if(action == MenuAction.EXAMINE_NPC && entry.npc != null) {
            entry.setTarget(entry.target + " (${entry.npc!!.id})")
        } else if(action == MenuAction.EXAMINE_OBJECT || action == MenuAction.EXAMINE_ITEM_GROUND) {
            entry.setTarget(entry.target + " (${event.identifier})")
        } else if(action == MenuAction.EXAMINE_ITEM) {
            entry.setTarget(entry.target + " (${event.identifier})")
        }
    }

    @Subscribe
    fun onGameStateChanged(event: GameStateChanged) {
        if (event.gameState == GameState.LOGIN_SCREEN && (firstLogin || config.loginAfterDisconnect())) {
            accountManager.getDefaultAccount()?.let { account ->
                scriptManager.runTask(LoginTask(account), client)
                firstLogin = false
            }
        }
    }

    private fun setState(state: BotState) {
        this.state = state
        eventBus.post(BotStateChanged(state))
    }
}