package net.tacobuddies.bot.helpers

import kotlinx.coroutines.delay
import net.tacobuddies.bot.scriptable.ContextProvider
import net.tacobuddies.bot.scriptable.TaskContext
import java.awt.Canvas
import java.awt.Component
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import kotlin.random.Random

class Keyboard(context: TaskContext) : ContextProvider<TaskContext>(context) {
    private val client = context.client

    suspend fun sendKeys(str: String, sendEnter: Boolean) {
        for(char in str.toCharArray()) {
            sendKey(char)
            delay(Random.nextLong(10, 30))
        }

        if(sendEnter) {
            sendKey(KeyEvent.VK_ENTER.toChar())
        }
    }

    suspend fun sendKey(char: Char) {
        var shift = false
        var code = char.code
        if(char in 'a'..'z') {
            code -= 32
        } else if (char in 'A' .. 'Z') {
            shift = true
        }

        if(code in arrayOf(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN)) {
            pressKey(KeyEvent(
                SOURCE,
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                0,
                code,
                keyCharFor(char),
                KeyEvent.KEY_LOCATION_STANDARD
            ))

            delay(Random.nextLong(25, 50))

            releaseKey(KeyEvent(
                SOURCE,
                KeyEvent.KEY_RELEASED,
                System.currentTimeMillis(),
                0,
                code,
                keyCharFor(char),
                KeyEvent.KEY_LOCATION_STANDARD
            ))
        } else {
            if(shift) {
                pressKey(KeyEvent(
                    SOURCE,
                    KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(),
                    InputEvent.SHIFT_DOWN_MASK,
                    KeyEvent.VK_SHIFT,
                    KeyEvent.CHAR_UNDEFINED,
                    KeyEvent.KEY_LOCATION_LEFT
                ))
                delay(Random.nextLong(10, 20))
            }

            pressKey(KeyEvent(
                SOURCE,
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                if(shift) { InputEvent.SHIFT_DOWN_MASK } else { 0 },
                code,
                keyCharFor(char),
                KeyEvent.KEY_LOCATION_STANDARD
            ))

            typedKey(KeyEvent(
                SOURCE,
                KeyEvent.KEY_TYPED,
                System.currentTimeMillis(),
                if(shift) { InputEvent.SHIFT_DOWN_MASK } else { 0 },
                0,
                keyCharFor(char),
                0
            ))

            delay(Random.nextLong(25, 50))

            releaseKey(KeyEvent(
                SOURCE,
                KeyEvent.KEY_RELEASED,
                System.currentTimeMillis(),
                if(shift) { InputEvent.SHIFT_DOWN_MASK } else { 0 },
                code,
                keyCharFor(char),
                KeyEvent.KEY_LOCATION_STANDARD
            ))

            if(shift) {
                delay(Random.nextLong(10, 20))
                releaseKey(KeyEvent(
                    SOURCE,
                    KeyEvent.KEY_RELEASED,
                    System.currentTimeMillis(),
                    InputEvent.SHIFT_DOWN_MASK,
                    KeyEvent.VK_SHIFT,
                    KeyEvent.CHAR_UNDEFINED,
                    KeyEvent.KEY_LOCATION_LEFT
                ))
            }
        }
    }

    fun pressKey(char: Char) {
        client.canvas.keyListeners.forEach {
            it.keyPressed(KeyEvent(
                SOURCE,
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                0,
                char.code,
                keyCharFor(char)
            ))
        }
    }

    fun releaseKey(char: Char) {
        client.canvas.keyListeners.forEach {
            it.keyReleased(KeyEvent(
                SOURCE,
                KeyEvent.KEY_RELEASED,
                System.currentTimeMillis(),
                0,
                char.code,
                keyCharFor(char)
            ))
        }
    }

    private fun dispatchEvent(event: KeyEvent) {
        client.canvas.dispatchEvent(event)
    }

    private fun pressKey(event: KeyEvent) {
        client.canvas.keyListeners.forEach {
            it.keyPressed(event)
        }
    }

    private fun releaseKey(event: KeyEvent) {
        client.canvas.keyListeners.forEach {
            it.keyReleased(event)
        }
    }

    private fun typedKey(event: KeyEvent) {
        client.canvas.keyListeners.forEach {
            it.keyTyped(event)
        }
    }

    companion object {
        val SOURCE: Component = Canvas()

        fun keyCharFor(char: Char): Char {
            if(char >= 36.toChar() && char <= 40.toChar()) {
                return KeyEvent.CHAR_UNDEFINED
            } else {
                return char
            }
        }
    }
}