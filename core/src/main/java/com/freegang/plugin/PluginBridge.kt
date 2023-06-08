package com.freegang.plugin

import android.annotation.SuppressLint
import android.app.Application
import android.app.Instrumentation
import android.content.ComponentName
import com.freegang.ktutils.reflect.findField
import com.freegang.ktutils.reflect.findFieldAndGet

object PluginBridge {
    fun init(application: Application, stubActivityClass: Class<*>) {
        hookPackageManager(application, stubActivityClass)
        hookInstrumentation(application, stubActivityClass)
    }

    /**
     * 动态代理 Instrumentation
     */
    @SuppressLint("PrivateApi")
    private fun hookInstrumentation(
        application: Application,
        subActivityClass: Class<*>,
    ) {
        // 获取到 ActivityThread 中的静态字段 sCurrentActivityThread 即是它自身的实例对象
        val atClazz = Class.forName("android.app.ActivityThread")
        val sCurrentActivityThreadFiled = atClazz.findField("sCurrentActivityThread")
        val sCurrentActivityThread = sCurrentActivityThreadFiled.get(null) ?: return

        val mInstrumentationFiled = atClazz.findField("mInstrumentation")
        val mInstrumentation = sCurrentActivityThread.findFieldAndGet("mInstrumentation") as Instrumentation

        //替换
        mInstrumentationFiled.set(sCurrentActivityThread, PluginInstrumentation(mInstrumentation, subActivityClass))
    }

    /**
     * 未注册的Activity代理替换后虽然能够成功跳转, 但是会有一个[android.content.pm.PackageManager.NameNotFoundException]异常.
     * 在 Android 13 中会有一个[android.app.ApplicationPackageManager#getActivityInfo()]获取到ActivityInfo的操作(低版本不一样, 未作适配),
     * 这里需要再对其进行欺骗
     */
    @SuppressLint("PrivateApi")
    private fun hookPackageManager(
        application: Application,
        subActivityClass: Class<*>,
    ) {
        try {
            // 获取到 ActivityThread 中的静态字段 sPackageManager 一个静态的 IPackageManager
            val atClazz = Class.forName("android.app.ActivityThread")
            val sPackageManagerField = atClazz.findField("sPackageManager")
            val sPackageManager = sPackageManagerField.get(null)

            //动态代理 IPackageManager
            val ipm = Class.forName("android.content.pm.IPackageManager")
            val proxy = proxySingle(sPackageManager!!, ipm.classLoader!!, ipm) { _, method, args ->
                if (method.name == "getActivityInfo") {
                    args?.forEachIndexed { index, _ ->
                        if (args[index] is ComponentName) {
                            args[index] = ComponentName(application.packageName, subActivityClass.name)
                        }
                    }
                }
            }

            //替换
            sPackageManagerField.set(sPackageManager, proxy)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}