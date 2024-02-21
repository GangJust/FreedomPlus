package io.github.fplus.plugin.proxy.v1

import android.annotation.SuppressLint
import android.app.Application
import android.app.Instrumentation
import android.content.ComponentName
import io.github.fplus.plugin.proxySingle

object PluginBridge {
    fun init(application: Application, stubActivity: Class<*>) {
        init(application, stubActivity.name)
    }

    fun init(application: Application, stubActivity: String) {
        hookPackageManager(application, stubActivity)
        hookInstrumentation(application, stubActivity)
    }

    /**
     * 动态代理 Instrumentation
     * @url https://github.com/androidmalin/AndroidComponentPlugin/blob/develop_kotlin/pluingImpl/src/main/java/com/malin/plugin/impl/HookInstrumentation.kt#L32
     */
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private fun hookInstrumentation(
        application: Application,
        subActivityClassName: String,
    ) {
        // 1.from ContextImpl get mMainThread field value (ActivityThread obj)
        // 2.from ActivityThread get mInstrumentation field (Instrumentation obj)
        // 3.replace ActivityThread  mInstrumentation field value use make a Instrumentation instance
        try {
            // 1.ContextImpl-->mMainThread
            // package android.app
            // class ContextImpl
            val contextImplClazz = Class.forName("android.app.ContextImpl")

            // final @NonNull ActivityThread mMainThread;
            val mMainThreadField = contextImplClazz.getDeclaredField("mMainThread").also { it.isAccessible = true }

            // 2.get ActivityThread Object from ContextImpl
            val activityThreadObj = mMainThreadField.get(application.baseContext)

            // 3.mInstrumentation Object
            val activityThreadClazz = Class.forName("android.app.ActivityThread")

            // Instrumentation mInstrumentation;
            val mInstrumentationField = activityThreadClazz.getDeclaredField("mInstrumentation")
                .also { it.isAccessible = true }
            val mInstrumentationObj = mInstrumentationField.get(activityThreadObj) as Instrumentation

            // 4.reset set value
            mInstrumentationField.set(activityThreadObj, PluginInstrumentation(mInstrumentationObj, subActivityClassName))
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    /**
     * 未注册的Activity代理替换后虽然能够成功跳转, 但是会有一个[android.content.pm.PackageManager.NameNotFoundException]异常.
     * 在 Android 13 中会有一个[android.app.ApplicationPackageManager#getActivityInfo()]获取到ActivityInfo的操作(低版本不一样, 未作适配),
     * 这里需要再对其进行欺骗
     */
    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    private fun hookPackageManager(
        application: Application,
        stubActivityClassName: String,
    ) {
        try {
            // 获取到 ActivityThread 中的静态字段 sPackageManager 一个静态的 IPackageManager
            val atClazz = Class.forName("android.app.ActivityThread")
            val sPackageManagerField = atClazz.getDeclaredField("sPackageManager")
                .also { it.isAccessible = true }
            val sPackageManager = sPackageManagerField.get(null)

            // 动态代理 IPackageManager
            val ipm = Class.forName("android.content.pm.IPackageManager")
            val proxy = proxySingle(sPackageManager!!, ipm.classLoader!!, ipm) { _, method, args ->
                if (method.name == "getActivityInfo") {
                    args?.forEachIndexed { index, _ ->
                        if (args[index] is ComponentName) {
                            args[index] = ComponentName(application.packageName, stubActivityClassName)
                        }
                    }
                }
            }

            // 替换
            sPackageManagerField.set(sPackageManager, proxy)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}