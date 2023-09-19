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
        return mBase.newApplication(cl, className, context)
    }

    override fun callApplicationOnCreate(app: Application?) {
        mBase.callApplicationOnCreate(app)
    }

    override fun callActivityOnCreate(activity: Activity?, icicle: Bundle?) {
        mBase.callActivityOnCreate(activity, icicle)
    }

    override fun callActivityOnCreate(activity: Activity?, icicle: Bundle?, persistentState: PersistableBundle?) {
        mBase.callActivityOnCreate(activity, icicle, persistentState)
    }

    override fun callActivityOnDestroy(activity: Activity?) {
        mBase.callActivityOnDestroy(activity)
    }

    override fun callActivityOnRestoreInstanceState(activity: Activity, savedInstanceState: Bundle) {
        mBase.callActivityOnRestoreInstanceState(activity, savedInstanceState)
    }

    override fun callActivityOnRestoreInstanceState(
        activity: Activity,
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        mBase.callActivityOnRestoreInstanceState(activity, savedInstanceState, persistentState)
    }

    override fun callActivityOnPostCreate(activity: Activity, savedInstanceState: Bundle?) {
        mBase.callActivityOnPostCreate(activity, savedInstanceState)
    }

    override fun callActivityOnPostCreate(activity: Activity, savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        mBase.callActivityOnPostCreate(activity, savedInstanceState, persistentState)
    }

    override fun callActivityOnNewIntent(activity: Activity?, intent: Intent?) {
        mBase.callActivityOnNewIntent(activity, intent)
    }

    override fun callActivityOnStart(activity: Activity?) {
        mBase.callActivityOnStart(activity)
    }

    override fun callActivityOnRestart(activity: Activity?) {
        mBase.callActivityOnRestart(activity)
    }

    override fun callActivityOnResume(activity: Activity?) {
        mBase.callActivityOnResume(activity)
    }

    override fun callActivityOnStop(activity: Activity?) {
        mBase.callActivityOnStop(activity)
    }

    override fun callActivityOnSaveInstanceState(activity: Activity, outState: Bundle) {
        mBase.callActivityOnSaveInstanceState(activity, outState)
    }

    override fun callActivityOnSaveInstanceState(activity: Activity, outState: Bundle, outPersistentState: PersistableBundle) {
        mBase.callActivityOnSaveInstanceState(activity, outState, outPersistentState)
    }

    override fun callActivityOnPause(activity: Activity?) {
        mBase.callActivityOnPause(activity)
    }

    override fun callActivityOnUserLeaving(activity: Activity?) {
        mBase.callActivityOnUserLeaving(activity)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun callActivityOnPictureInPictureRequested(activity: Activity) {
        mBase.callActivityOnPictureInPictureRequested(activity)
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
            //KLogCat.xposedLog(e)
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