package com.freegang.plugin

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.view.ContextThemeWrapper
import com.freegang.ktutils.log.KLogCat

class PluginContextThemeWrapper(
    context: Context,
    themeResId: Int = 0,
) : ContextThemeWrapper(
    context,
    themeResId,
) {
    private val mResources by lazy {
        PluginResource(super.getResources())
    }

    private val mTheme by lazy {
        val newTheme = mResources.newTheme()
        newTheme.also { it.setTo(super.getTheme()) }
    }

    override fun getResources(): Resources {
        return mResources
    }

    override fun getAssets(): AssetManager {
        return mResources.assets
    }

    override fun getTheme(): Resources.Theme {
        return mTheme
    }
}