package com.freegang.fplus

import android.app.Application
import com.freegang.xpler.utils.app.KAppCrashUtils

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        KAppCrashUtils.instance.init(this, "Freedom+崩溃退出!")
    }
}