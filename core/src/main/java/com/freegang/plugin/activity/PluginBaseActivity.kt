package com.freegang.plugin.activity

import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.freegang.ktutils.reflect.findMethodAndInvoke
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.loader.hostClassloader
import com.freegang.xpler.loader.moduleClassloader

open class PluginBaseActivity : ComponentActivity() {

    private val mClassLoader by lazy {
        PluginClassloader()
    }

    private val mResources by lazy {
        val assetManager = AssetManager::class.java.newInstance()
        assetManager.findMethodAndInvoke("addAssetPath", KtXposedHelpers.modulePath)
        Resources(assetManager, super.getResources().displayMetrics, super.getResources().configuration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar)
        super.onCreate(savedInstanceState)
    }

    override fun getClassLoader(): ClassLoader = mClassLoader

    override fun getResources(): Resources = mResources

    private class PluginClassloader : ClassLoader() {
        override fun findClass(name: String?): Class<*> {
            //KLogCat.d("插件加载类: $name")
            try {
                return moduleClassloader!!.loadClass(name)
            } catch (e: Exception) {
                //KLogCat.d("模块未找到: $name")
            }
            try {
                return hostClassloader!!.loadClass(name)
            } catch (e: Exception) {
                //KLogCat.d("宿主未找到: $name")
            }
            return super.findClass(name)
        }
    }
}