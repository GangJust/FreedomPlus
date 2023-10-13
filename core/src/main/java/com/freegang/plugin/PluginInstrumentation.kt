package com.freegang.plugin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.classLoader
import com.freegang.xpler.core.xposedLog
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
        return try {
            mBase.newApplication(cl, className, context)
        } catch (e: Exception) {
            super.newApplication(cl, className, context)
        }
    }

    override fun callApplicationOnCreate(app: Application?) {
        try {
            mBase.callApplicationOnCreate(app)
        } catch (e: Exception) {
            super.callApplicationOnCreate(app)
        }
    }

    override fun callActivityOnCreate(activity: Activity?, icicle: Bundle?) {
        try {
            injectRes(activity)
            mBase.callActivityOnCreate(activity, icicle)
        } catch (e: Exception) {
            super.callActivityOnCreate(activity, icicle)
        }
    }

    override fun callActivityOnCreate(activity: Activity?, icicle: Bundle?, persistentState: PersistableBundle?) {
        try {
            injectRes(activity)
            mBase.callActivityOnCreate(activity, icicle, persistentState)
        } catch (e: Exception) {
            super.callActivityOnCreate(activity, icicle, persistentState)
        }
    }

    override fun callActivityOnDestroy(activity: Activity?) {
        try {
            mBase.callActivityOnDestroy(activity)
        } catch (e: Exception) {
            super.callActivityOnDestroy(activity)
        }
    }

    override fun callActivityOnRestoreInstanceState(activity: Activity, savedInstanceState: Bundle) {
        try {
            mBase.callActivityOnRestoreInstanceState(activity, savedInstanceState)
        } catch (e: Exception) {
            super.callActivityOnRestoreInstanceState(activity, savedInstanceState)
        }
    }

    override fun callActivityOnRestoreInstanceState(
        activity: Activity,
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        try {
            mBase.callActivityOnRestoreInstanceState(activity, savedInstanceState, persistentState)
        } catch (e: Exception) {
            super.callActivityOnRestoreInstanceState(activity, savedInstanceState, persistentState)
        }
    }

    override fun callActivityOnPostCreate(activity: Activity, savedInstanceState: Bundle?) {
        try {
            mBase.callActivityOnPostCreate(activity, savedInstanceState)
        } catch (e: Exception) {
            super.callActivityOnPostCreate(activity, savedInstanceState)
        }
    }

    override fun callActivityOnPostCreate(
        activity: Activity,
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        try {
            mBase.callActivityOnPostCreate(activity, savedInstanceState, persistentState)
        } catch (e: Exception) {
            super.callActivityOnPostCreate(activity, savedInstanceState, persistentState)
        }
    }

    override fun callActivityOnNewIntent(activity: Activity?, intent: Intent?) {
        try {
            mBase.callActivityOnNewIntent(activity, intent)
        } catch (e: Exception) {
            super.callActivityOnNewIntent(activity, intent)
        }
    }

    override fun callActivityOnStart(activity: Activity?) {
        try {
            mBase.callActivityOnStart(activity)
        } catch (e: Exception) {
            super.callActivityOnStart(activity)
        }
    }

    override fun callActivityOnRestart(activity: Activity?) {
        try {
            mBase.callActivityOnRestart(activity)
        } catch (e: Exception) {
            super.callActivityOnRestart(activity)
        }
    }

    override fun callActivityOnResume(activity: Activity?) {
        try {
            mBase.callActivityOnResume(activity)
        } catch (e: Exception) {
            super.callActivityOnResume(activity)
        }
    }

    override fun callActivityOnStop(activity: Activity?) {
        try {
            mBase.callActivityOnStop(activity)
        } catch (e: Exception) {
            super.callActivityOnStop(activity)
        }
    }

    override fun callActivityOnSaveInstanceState(activity: Activity, outState: Bundle) {
        try {
            mBase.callActivityOnSaveInstanceState(activity, outState)
        } catch (e: Exception) {
            super.callActivityOnSaveInstanceState(activity, outState)
        }
    }

    override fun callActivityOnSaveInstanceState(
        activity: Activity,
        outState: Bundle,
        outPersistentState: PersistableBundle
    ) {
        try {
            mBase.callActivityOnSaveInstanceState(activity, outState, outPersistentState)
        } catch (e: Exception) {
            super.callActivityOnSaveInstanceState(activity, outState, outPersistentState)
        }
    }

    override fun callActivityOnPause(activity: Activity?) {
        try {
            mBase.callActivityOnPause(activity)
        } catch (e: Exception) {
            super.callActivityOnPause(activity)
        }
    }

    override fun callActivityOnUserLeaving(activity: Activity?) {
        try {
            mBase.callActivityOnUserLeaving(activity)
        } catch (e: Exception) {
            super.callActivityOnUserLeaving(activity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun callActivityOnPictureInPictureRequested(activity: Activity) {
        try {
            mBase.callActivityOnPictureInPictureRequested(activity)
        } catch (e: Exception) {
            super.callActivityOnPictureInPictureRequested(activity)
        }
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
                    KLogCat.xposedLog(e)
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