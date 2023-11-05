package com.freegang.plugin.v1

import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.compose.setContent
import com.freegang.ktutils.reflect.methodInvokeFirst
import com.freegang.plugin.PluginClassloader
import com.freegang.plugin.base.BaseXplerActivity
import com.freegang.xpler.core.KtXposedHelpers

open class XplerActivity : BaseXplerActivity() {

    private var mClassLoader: ClassLoader? = null

    private var mResources: Resources? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // 方案一: 资源代理在Android 10上宿主无法找到 android.content.res.loader.ResourcesLoader (而这个错误是抖音内反射调用的报错, 猜测是Classloader出的问题)
        // 方案二: 资源合并在抖音启动时有一个 layout/_ 资源的报错崩溃。
        // 方案三: 方案一的基础上不对宿主资源进行代理, 插件只使用自身资源, 本质上都是创建新的资源, 能跑但不知道为什么, 每隔一段时间都会出现资源找不到的情况(而方案一却不会)。
        mClassLoader = PluginClassloader()
        mResources = pluginResource()
        setTheme(com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar)
        super.onCreate(savedInstanceState)
    }

    override fun getClassLoader(): ClassLoader = mClassLoader ?: super.getClassLoader()

    override fun getResources(): Resources = mResources ?: super.getResources()

    override fun getAssets(): AssetManager = mResources?.assets ?: super.getAssets()

    override fun getTheme(): Resources.Theme = mResources?.newTheme() ?: super.getTheme()

    private fun pluginResource(): Resources {
        val originResources = super.getResources()
        return if (KtXposedHelpers.modulePath.isEmpty()) {
            originResources
        } else {
            val assetManager = AssetManager::class.java.newInstance()
            assetManager.methodInvokeFirst("addAssetPath", args = arrayOf(KtXposedHelpers.modulePath))
            Resources(assetManager, originResources.displayMetrics, originResources.configuration)
        }
    }
}