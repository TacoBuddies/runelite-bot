package net.tacobuddies.bot

import net.runelite.cache.fs.Store

interface CacheProvider<T> {
    fun provideDefinition(id: Int): T?
}