package com.freegang.plugin.activity

import android.content.res.AssetManager
import android.content.res.Resources
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.findMethodAndInvoke
import com.freegang.xpler.R
import com.freegang.xpler.core.KtXposedHelpers

object PluginResourceBridge {
    fun injectRes(res: Resources) {
        res.assets.findMethodAndInvoke("addAssetPath", KtXposedHelpers.modulePath)
        /*try {
            KLogCat.d("资源: ${res.getString(R.string.inject_res_hint)}")
        } catch (e: Exception) {
            KLogCat.d("资源注入失败!")
        }*/
    }

    fun testRes(res: Resources) {
        val assetManager = AssetManager::class.java.newInstance()
        assetManager.findMethodAndInvoke("addAssetPath", KtXposedHelpers.modulePath)
        val moduleRes = Resources(assetManager, res.displayMetrics, res.configuration)
        try {
            KLogCat.d("插件资源: ${moduleRes.getString(R.string.inject_res_hint)}")
        } catch (e: Exception) {
            KLogCat.d("资源获取失败!")
        }

        try {
            KLogCat.d("宿主资源: ${res.getString(R.string.inject_res_hint)}")
        } catch (e: Exception) {
            KLogCat.d("宿主获取失败!")
            throw e
        }
    }
}