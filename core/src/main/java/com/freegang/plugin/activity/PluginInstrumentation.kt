package com.freegang.plugin.activity

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.text.TextUtils
import com.freegang.xpler.loader.moduleClassloader

class PluginInstrumentation(private val mBase: Instrumentation) : Instrumentation() {
    private val TAG = "PluginBridge"
    override fun newActivity(cl: ClassLoader, className: String, intent: Intent): Activity {
        //Log.d(TAG, "newActivity-> cl: $cl, class: $className, intent: $intent")
        val isLoader = intent.getStringExtra(PluginActivityBridge.ORIGINAL_INTENT_KEY)
        if (!TextUtils.isEmpty(isLoader)) {
            val activity = moduleClassloader!!.loadClass(className).newInstance() as Activity
            activity.intent = intent
            return activity
        }
        return mBase.newActivity(cl, className, intent)
    }
}