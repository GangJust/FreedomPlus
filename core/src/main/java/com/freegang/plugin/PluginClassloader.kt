package com.freegang.plugin

import com.freegang.ktutils.log.KLogCat
import com.freegang.xpler.loader.hostClassloader
import com.freegang.xpler.loader.moduleClassloader

class PluginClassloader : ClassLoader() {
    override fun findClass(name: String?): Class<*> {
        val loadedClass = findLoadedClass(name)
        if (loadedClass != null) return loadedClass

        try {
            return moduleClassloader!!.loadClass(name)
        } catch (e: Exception) {
            // KLogCat.e(e)
        }
        try {
            return hostClassloader!!.loadClass(name)
        } catch (e: Exception) {
            // KLogCat.e(e)
        }

        return super.findClass(name)
    }
}