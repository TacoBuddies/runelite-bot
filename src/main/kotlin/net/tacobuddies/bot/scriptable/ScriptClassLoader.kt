package net.tacobuddies.bot.scriptable

import net.runelite.client.util.ReflectUtil
import net.runelite.client.util.ReflectUtil.PrivateLookupableClassLoader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.invoke.MethodHandles
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader

class ScriptClassLoader(private val base: URL) : URLClassLoader(arrayOf(base), ScriptClassLoader::class.java.classLoader), PrivateLookupableClassLoader {
    private var lookup: MethodHandles.Lookup? = null

    init {
        ReflectUtil.installLookupHelper(this)
    }

    override fun getLookup(): MethodHandles.Lookup? {
        return lookup
    }

    override fun setLookup(p0: MethodHandles.Lookup?) {
        this.lookup = p0
    }

    @Throws(ClassNotFoundException::class)
    public override fun loadClass(name: String, resolve: Boolean): Class<*>? {
        var clazz = findLoadedClass(name)

        if (clazz == null) {
            try {
                val `in` = getResourceAsStream(name.replace('.', '/') + ".class")
                    ?: return super.loadClass(name, resolve)

                val buffer = ByteArray(4096)
                val out = ByteArrayOutputStream()

                var n: Int
                while ((`in`.read(buffer, 0, 4096).also { n = it }) != -1) {
                    out.write(buffer, 0, n)
                }

                val bytes = out.toByteArray()
                clazz = defineClass(name, bytes, 0, bytes.size)

                if (resolve) {
                    resolveClass(clazz)
                }

            } catch (e: Exception) {
                clazz = super.loadClass(name, resolve)
            }
        }

        return clazz
    }

    override fun getResource(name: String): URL? {
        return try {
            URL(base, name)
        } catch (e: MalformedURLException) {
            null
        }
    }

    override fun getResourceAsStream(name: String): InputStream? {
        return try {
            URL(base, name).openStream()
        } catch (e: IOException) {
            null
        }
    }

    @Throws(ClassFormatError::class)
    override fun defineClass0(name: String, b: ByteArray, off: Int, len: Int): Class<*> {
        return super.defineClass(name, b, off, len)
    }
}