package com.freegang.plugin.activity

import android.annotation.SuppressLint
import android.app.Application
import android.app.Instrumentation
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.util.Log
import com.freegang.ktutils.reflect.findField
import com.freegang.ktutils.reflect.findFieldAndGet
import com.freegang.ktutils.reflect.findMethodAndInvoke
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object PluginActivityBridge {
    const val TAG = "PluginBridge"
    const val ORIGINAL_INTENT_KEY = "original"
    const val PLUGIN_PROXY_ACTIVITY = "com.ss.android.ugc.aweme.setting.ui.AboutActivity"

    private var mApplication: Application? = null
    val application get() = mApplication!!

    @SuppressLint("PrivateApi")
    fun init(application: Application) {
        this.mApplication = application
        replaceInstrumentation()
        replaceHandlerCallback()
        replaceActivityManager()
        hookPackageManager()
    }

    @SuppressLint("PrivateApi")
    private fun replaceInstrumentation() {
        // 获取到 ActivityThread 中的静态字段 sCurrentActivityThread 即是它自身的实例对象
        val atClazz = Class.forName("android.app.ActivityThread")
        val sCurrentActivityThreadFiled = atClazz.findField("sCurrentActivityThread")
        val sCurrentActivityThread = sCurrentActivityThreadFiled.get(null)

        val mInstrumentationFiled = atClazz.findField("mInstrumentation")
        val mInstrumentation = sCurrentActivityThread!!.findFieldAndGet("mInstrumentation") as Instrumentation

        //替换
        mInstrumentationFiled.set(sCurrentActivityThread, PluginInstrumentation(mInstrumentation))
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    private fun replaceActivityManager() {
        try {
            val singletonField: Field = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                //Android 8-, 获取到 gDefault 字段
                val clazz = Class.forName("android.app.ActivityManagerNative")
                clazz.findField("gDefault")
            } else {
                //Android 8+, 获取到 IActivityTaskManagerSingleton 字段
                val clazz = Class.forName("android.app.ActivityTaskManager")
                clazz.findField("IActivityTaskManagerSingleton")
            }

            val singleton = singletonField.get(null)

            // 获取到 mInstance 即是 IActivityTaskManager 的实例单例对象
            val singletonClazz = Class.forName("android.util.Singleton")
            val mInstanceField = singletonClazz.findField("mInstance")
            var mInstance = mInstanceField.get(singleton)

            //如果 mInstance 为空, 则主动调用一下 android.util.Singleton#get() 方法获取
            //这种情况通常发生在 Application#onCrate() 方法下
            if (mInstance == null) {
                mInstance = singleton!!.findMethodAndInvoke("get")
            }

            // 代理 IActivityTaskManager 类
            val atmClazz = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                //Android 9- 有一个灰名单限制
                Class.forName("android.app.IActivityManager")
            } else {
                Class.forName("android.app.IActivityTaskManager")
            }
            val mInstanceProxy =
                proxySingle(mInstance, atmClazz.classLoader!!, atmClazz) { _, method, args ->
                    if (method.name == "startActivity") {
                        val anies = args ?: emptyArray()
                        for (index in anies.indices) {
                            val item = anies[index]
                            if (item !is Intent) continue
                            if (!isModuleActivity(item.component?.className)) continue
                            if (isRegistered(item)) continue

                            //将原始的activity保存下来, 为后续对消息达成欺骗
                            //替换原始intent为目标intent
                            val proxyIntent = Intent()
                            proxyIntent.component = ComponentName(application.packageName, PLUGIN_PROXY_ACTIVITY)
                            proxyIntent.putExtra(ORIGINAL_INTENT_KEY, item)
                            args!![index] = proxyIntent
                            Log.d(TAG, "replaceActivityManager: 替换成功!")
                        }
                    }
                }

            // 替换
            mInstanceField.set(singleton, mInstanceProxy)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 对 ActivityThread 的 Handler-> mH 消息欺骗, 完成 intent 替换
     */
    @SuppressLint("PrivateApi")
    private fun replaceHandlerCallback() {
        try {
            // 获取到 ActivityThread 中的静态字段 sCurrentActivityThread 即是它自身的实例对象
            val atClazz = Class.forName("android.app.ActivityThread")
            val sCurrentActivityThreadField = atClazz.findField("sCurrentActivityThread")
            val sCurrentActivityThread = sCurrentActivityThreadField.get(null)

            //获取到 mH 消息实现
            val mHField = atClazz.findField("mH")
            val mH = mHField.get(sCurrentActivityThread)

            //获取到Handler类中的 callback 字段
            val mCallbackField = Handler::class.java.findField("mCallback")

            //对 callback 进行重写
            mCallbackField.set(mH, PluginHandlerCallback())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 未注册的Activity代理替换后虽然能够成功跳转, 但是会有一个[android.content.pm.PackageManager.NameNotFoundException]异常.
     * 在 Android 13 中会有一个[android.app.ApplicationPackageManager#getActivityInfo()]获取到ActivityInfo的操作(低版本不一样, 未作适配),
     * 这里需要再对其进行欺骗
     */
    @SuppressLint("PrivateApi")
    fun hookPackageManager() {
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
                            args[index] = ComponentName(application.packageName, PLUGIN_PROXY_ACTIVITY)
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

    /**
     * Activity是否已经注册 Manifest
     */
    private fun isRegistered(intent: Intent): Boolean {
        val queryIntentActivities = application.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return queryIntentActivities.isNotEmpty()
    }

    /**
     * 是否为模块中的Activity
     */
    private fun isModuleActivity(name: String?): Boolean {
        return name?.startsWith("com.freegang") ?: return false
    }

    /**
     * 单个类的代理处理
     */
    private fun proxySingle(
        instance: Any,
        loader: ClassLoader,
        clazz: Class<*>,
        block: (proxy: Any, method: Method, args: Array<Any>?) -> Unit,
    ): Any {
        if (!clazz.isInstance(instance)) {
            throw IllegalArgumentException("`Instance` should be the implementation class of `clazz`.")
        }

        return Proxy.newProxyInstance(loader, arrayOf(clazz)) { proxy, method, args ->
            block.invoke(proxy, method, args)
            //参数为空, 则调用 `invoke(mInstance` 否则, 调用 method.invoke(mInstance, *args)
            val result = if (args == null) method.invoke(instance) else method.invoke(instance, *args)
            //返回值为Void, 直接返回 Unit
            if (method.genericReturnType == Void.TYPE) Unit else result
        }
    }
}