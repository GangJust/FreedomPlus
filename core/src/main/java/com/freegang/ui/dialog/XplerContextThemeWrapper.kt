package com.freegang.ui.dialog

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.view.ContextThemeWrapper
import com.freegang.ktutils.reflect.methodInvokes
import com.freegang.xpler.core.KtXposedHelpers

class XplerContextThemeWrapper(context: Context) : ContextThemeWrapper(context, 0) {
    private val mResources by lazy {
        val originResources = super.getResources()
        if (KtXposedHelpers.modulePath.isEmpty()) {
            return@lazy originResources
        } else {
            val assetManager = AssetManager::class.java.newInstance()
            assetManager.methodInvokes("addAssetPath", args = arrayOf(KtXposedHelpers.modulePath))
            Resources(assetManager, originResources.displayMetrics, originResources.configuration)
        }
    }

    override fun getResources(): Resources {
        return mResources
    }

    override fun getAssets(): AssetManager {
        return mResources.assets
    }

    override fun getTheme(): Resources.Theme {
        return mResources.newTheme()
    }
}