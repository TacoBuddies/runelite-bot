package net.tacobuddies.bot.scriptable

import net.runelite.api.Client
import net.runelite.cache.ItemManager
import net.runelite.cache.NpcManager
import net.runelite.cache.ObjectManager
import net.tacobuddies.bot.account.AccountManager
import net.tacobuddies.bot.helpers.*

class ScriptContext(
    client: Client,
    val objectManager: ObjectManager,
    val npcManager: NpcManager,
    val itemManager: ItemManager,
    val accountManager: AccountManager
) : TaskContext(client) {

    val calculations = Calculations(this)
    val game = Game(this)
    val gameUI = GameUI(this)
    val camera = Camera(this)
    val interfaces = Interfaces(this)
    val menu = Menu(this)
    val players = Players(this)
    val npcs = Npcs(this)
    val objects = Objects(this)
    val groundItems = GroundItems(this)
    val inventory = Inventory(this)
    val equipment = Equipment(this)

}