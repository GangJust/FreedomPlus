package com.freegang.fplus

import android.app.Application
import com.freegang.fplus.activity.ErrorActivity
import com.freegang.xpler.utils.app.KAppCrashUtils
import com.freegang.xpler.utils.log.KLogCat

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        KLogCat.init(this)
        KLogCat.openStorage()
        KAppCrashUtils.instance.init(this, ErrorActivity::class.java, "Freedom+崩溃退出!")
    }
}