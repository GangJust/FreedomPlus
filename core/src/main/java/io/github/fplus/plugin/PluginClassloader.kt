package io.github.fplus.plugin

import io.github.xpler.loader.hostClassloader
import io.github.xpler.loader.moduleClassloader

class PluginClassloader : ClassLoader() {
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        // Log.d("XplerLog", "load: $name")

        val loadedClass = findLoadedClass(name)
        if (loadedClass != null) return loadedClass

        try {
            return moduleClassloader!!.loadClass(name)
        } catch (e: ClassNotFoundException) {
            // e.printStackTrace()
        }
        try {
            return hostClassloader!!.loadClass(name)
        } catch (e: ClassNotFoundException) {
            // e.printStackTrace()
        }

        return super.loadClass(name, resolve)
    }
}