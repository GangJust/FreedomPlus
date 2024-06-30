package io.github.fplus.plugin

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.view.ContextThemeWrapper

class PluginContextThemeWrapper(
    context: Context,
    themeResId: Int = 0,
) : ContextThemeWrapper(
    context,
    themeResId,
) {
    private val mResources by lazy {
        PluginResources(super.getResources())
    }

    /* private val mResources by lazy {
        val originResources = super.getResources()
        if (KtXposedHelpers.modulePath.isEmpty()) {
            KLogCat.d("未获取到模块路径!")
            originResources
        } else {
            val assetManager = AssetManager::class.java.newInstance()
            assetManager.methodCall<Any>(KtXposedHelpers.modulePath) {
                parameterTypes(listOf(String::class.java))
            }
            Resources(assetManager, originResources.displayMetrics, originResources.configuration)
        }
    } */

    private val mTheme by lazy {
        val newTheme = mResources.newTheme()
        newTheme.also { it.setTo(super.getTheme()) }
    }

    override fun getResources(): Resources {
        return mResources
    }

    override fun getAssets(): AssetManager {
        val resources = mResources
        if (resources is PluginResources?) {
            return resources?.pluginAssets ?: mResources?.assets ?: super.getAssets()
        }
        return resources?.assets ?: super.getAssets()
    }

    override fun getTheme(): Resources.Theme {
        return mTheme
    }
}