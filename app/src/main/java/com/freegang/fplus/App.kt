package com.freegang.fplus

import android.app.Application
import com.freegang.fplus.activity.ErrorActivity
import com.freegang.fplus.activity.StubActivity
import com.freegang.ktutils.app.KAppCrashUtils
import com.freegang.ktutils.log.KLogCat
import com.freegang.plugin.v1.PluginBridge

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        KLogCat.init(this)
        KLogCat.openStorage()
        KAppCrashUtils.instance.init(this, ErrorActivity::class.java, "Freedom+崩溃退出!")
        PluginBridge.init(this, StubActivity::class.java)
        // PluginBridgeV1.init(this, StubActivity::class.java)
    }
}