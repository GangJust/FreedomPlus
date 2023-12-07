package io.github.fplus

import android.app.Application
import io.github.fplus.activity.ErrorActivity
import com.freegang.ktutils.app.KAppCrashUtils
import com.freegang.ktutils.log.KLogCat
import io.github.fplus.activity.StubActivity
import io.github.fplus.plugin.proxy.v1.PluginBridge
import io.github.fplus.plugin.proxy.v2.PluginBridgeV2

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        KLogCat.init(this)
        KLogCat.openStorage()
        KAppCrashUtils.instance.init(this, ErrorActivity::class.java, "Freedom+崩溃退出!")
        // PluginBridge.init(this, StubActivity::class.java)
        // PluginBridgeV2.init(this, StubActivity::class.java)
    }
}