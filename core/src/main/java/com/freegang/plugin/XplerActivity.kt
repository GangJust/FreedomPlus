package com.freegang.plugin

import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.freegang.xpler.loader.hostClassloader
import com.freegang.xpler.loader.moduleClassloader

open class XplerActivity : ComponentActivity() {

    protected val mClassLoader by lazy {
        PluginClassloader()
    }

    protected val mResources by lazy {
        PluginResource(super.getResources())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar)
        super.onCreate(savedInstanceState)
    }

    override fun getClassLoader(): ClassLoader = mClassLoader

    override fun getResources(): Resources = mResources

    protected class PluginClassloader : ClassLoader() {
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
}