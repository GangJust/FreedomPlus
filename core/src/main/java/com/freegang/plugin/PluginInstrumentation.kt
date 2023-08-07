package com.freegang.plugin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.Keep
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.classLoader
import com.freegang.xpler.loader.moduleClassloader
import java.lang.reflect.Method

class PluginInstrumentation(
    private val mBase: Instrumentation,
    private val stubActivity: Class<*>,
) : Instrumentation() {
    companion object {
        const val PLUGIN_PROXY_ACTIVITY = "xpler_plugin"
    }

    @Keep
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return mBase.newApplication(cl, className, context)
    }

    override fun callApplicationOnCreate(app: Application?) {
        mBase.callApplicationOnCreate(app)
    }

    @Keep
    @SuppressLint("DiscouragedPrivateApi")
    fun execStartActivity(
        who: Context?,
        contextThread: IBinder?,
        token: IBinder?,
        target: Activity?,
        intent: Intent?,
        requestCode: Int,
        options: Bundle?
    ): ActivityResult? {
        try {
            var newIntent = intent
            if (intent?.component != null) {
                try {
                    val pluginClazz = pluginClassloader?.loadClass(intent.component?.className)
                    if (pluginClazz != null && XplerActivity::class.java.isAssignableFrom(pluginClazz)) {
                        newIntent = Intent(who, stubActivity)
                        intent.extras?.let { newIntent.putExtras(it) }
                        newIntent.putExtra(PLUGIN_PROXY_ACTIVITY, pluginClazz.name)
                    }
                } catch (e: Exception) {
                    KLogCat.e(e)
                }
            }

            val execStartActivity: Method = Instrumentation::class.java.getDeclaredMethod(
                "execStartActivity",
                Context::class.java,
                IBinder::class.java,
                IBinder::class.java,
                Activity::class.java,
                Intent::class.java,
                Int::class.javaPrimitiveType,
                Bundle::class.java
            )

            return execStartActivity.invoke(
                mBase,
                who,
                contextThread,
                token,
                target,
                newIntent,
                requestCode,
                options,
            ) as ActivityResult?
        } catch (e: Exception) {
            //KLogCat.e(e)
            throw e
        }
    }

    override fun newActivity(cl: ClassLoader?, className: String?, intent: Intent?): Activity {
        val xplerPlugin = intent?.getStringExtra(PLUGIN_PROXY_ACTIVITY) ?: ""
        if (xplerPlugin.isNotEmpty() && pluginClassloader != null) {
            return pluginClassloader!!.loadClass(xplerPlugin).newInstance() as Activity
        }
        return mBase.newActivity(cl, className, intent)
    }

    private val pluginClassloader: ClassLoader?
        get() = moduleClassloader ?: PluginInstrumentation.classLoader
}