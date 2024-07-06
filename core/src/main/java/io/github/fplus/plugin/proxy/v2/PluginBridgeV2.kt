package io.github.fplus.plugin.proxy.v2

import android.annotation.SuppressLint
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Handler
import android.os.Message
import com.freegang.extension.classLoader
import com.freegang.ktutils.log.KLogCat
import io.github.fplus.plugin.base.IPluginActivity
import io.github.xpler.loader.moduleClassloader
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy

// see at: https://github.com/androidmalin/AndroidComponentPlugin
object PluginBridgeV2 {
    const val TAG = "PluginBridgeV2"

    private const val XPLER_ORIGIN_INTENT = "XPLER_ORIGIN_INTENT"

    fun init(application: Application, stubActivity: Class<*>) {
        init(application, stubActivity.name)
    }

    fun init(application: Application, stubActivity: String) {
        hookStartActivity(application, stubActivity)
        hookLauncherActivity()
        hookInstrumentation(application)
    }

    /**
     * 对IActivityManager接口中的startActivity方法进行动态代理,发生在app的进程中
     * [android.app.Activity.startActivity]
     * [android.app.Activity.startActivityForResult]
     * [android.app.Instrumentation]
     * android.app.Instrumentation#execStartActivity()
     * Activity#startActivityForResult-->Instrumentation#execStartActivity-->ActivityManager.getService().startActivity()-->
     * IActivityManager public int startActivity(android.app.IApplicationThread caller, java.lang.String callingPackage, android.content.Intent intent, java.lang.String resolvedType, android.os.IBinder resultTo, java.lang.String resultWho, int requestCode, int flags, android.app.ProfilerInfo profilerInfo, android.os.Bundle options) throws android.os.RemoteException;
     *
     * @param context          context
     * @param subActivityClazz 在AndroidManifest.xml中注册了的Activity
     */
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private fun hookStartActivity(
        context: Context,
        subActivityClassName: String,
    ) {
        // Android 10+
        // 1.获取ActivityTaskManager的Class对象
        // package android.app;
        // public class ActivityTaskManager
        val activityTaskManagerClazz = Class.forName("android.app.ActivityTaskManager")

        // 2.获取ActivityTaskManager的私有静态成员变量IActivityTaskManagerSingleton
        // private static final Singleton<IActivityTaskManager> IActivityTaskManagerSingleton
        // 3.取消Java的权限检查
        val iActivityTaskManagerSingletonField =
            activityTaskManagerClazz.getDeclaredField("IActivityTaskManagerSingleton").also { it.isAccessible = true }

        // 4.获取IActivityManagerSingleton的实例对象
        // private static final Singleton<IActivityTaskManager> IActivityTaskManagerSingleton
        // 所有静态对象的反射可以通过传null获取,如果是非静态必须传实例
        val iActivityTaskManagerSingletonObj = iActivityTaskManagerSingletonField.get(null)

        // 5.
        handleIActivityTaskManager(context, subActivityClassName, iActivityTaskManagerSingletonObj)
    }

    /**
     * handle 29 <= apiLevel <= 33
     * [android10 ... android13]
     */
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private fun handleIActivityTaskManager(
        context: Context,
        subActivityClassName: String,
        IActivityTaskManagerSingletonObj: Any?,
    ) {
        try {
            // 5.获取private static final Singleton<IActivityTaskManager> IActivityTaskManagerSingleton对象中的属性private T mInstance的值
            // 既,为了获取一个IActivityTaskManager的实例对象
            // private static final Singleton<IActivityTaskManager> IActivityTaskManagerSingleton = new Singleton<IActivityTaskManager>() {...}

            // 6.获取Singleton类对象
            // package android.util
            // public abstract class Singleton<T>
            val singletonClazz = Class.forName("android.util.Singleton")

            // 7.获取mInstance属性
            // private T mInstance;
            // 8.取消Java的权限检查
            val mInstanceField = singletonClazz.getDeclaredField("mInstance").also { it.isAccessible = true }

            // 9.获取mInstance属性的值,既IActivityTaskManager的实例
            // 从private static final Singleton<IActivityTaskManager> IActivityTaskManagerSingleton实例对象中获取mInstance属性对应的值,既IActivityTaskManager
            var iActivityTaskManager = mInstanceField[IActivityTaskManagerSingletonObj]

            // 10.android10之后,从mInstanceField中取到的值为null,这里判断如果为null,就再次从get方法中再取一次
            if (iActivityTaskManager == null) {
                val getMethod = singletonClazz.getDeclaredMethod("get").also { it.isAccessible = true }
                iActivityTaskManager = getMethod.invoke(IActivityTaskManagerSingletonObj)
            }

            // 11.获取IActivityTaskManager接口的类对象
            // package android.app
            // public interface IActivityTaskManager
            val iActivityTaskManagerClazz = Class.forName("android.app.IActivityTaskManager")

            // 12.创建一个IActivityTaskManager接口的代理对象
            val iActivityTaskManagerProxy = Proxy.newProxyInstance(
                Thread.currentThread().contextClassLoader,
                arrayOf(iActivityTaskManagerClazz),
                IActivityInvocationHandler(iActivityTaskManager, context, subActivityClassName)
            )

            // 13.重新赋值
            // 给mInstance属性,赋新值
            // 给Singleton<IActivityTaskManager> IActivityTaskManagerSingleton实例对象的属性private T mInstance赋新值
            mInstanceField[IActivityTaskManagerSingletonObj] = iActivityTaskManagerProxy
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    /**
     * 对IActivityManager/IActivityTaskManager接口进行动态代理
     */
    private class IActivityInvocationHandler(
        private val mIActivityManager: Any?,
        private val mContext: Context,
        private val mSubActivityClassName: String,
    ) : InvocationHandler {
        @Throws(InvocationTargetException::class, IllegalAccessException::class)
        override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
            if (method.name == "startActivity" && !args.isNullOrEmpty()) {
                var intentIndex = 2
                for (i in args.indices) {
                    if (args[i] is Intent) {
                        intentIndex = i
                        break
                    }
                }
                // 将启动的未注册的Activity对应的Intent,替换为安全的注册了的桩Activity的Intent
                // 1.将未注册的Activity对应的Intent,改为安全的Intent,既在AndroidManifest.xml中配置了的Activity的Intent
                val originIntent = args[intentIndex] as Intent
                val safeIntent = Intent().also { it.setClassName(mContext, mSubActivityClassName) }
                // public class Intent implements Parcelable;
                // Intent类已经实现了Parcelable接口
                safeIntent.putExtra(XPLER_ORIGIN_INTENT, originIntent)
                originIntent.extras?.runCatching { safeIntent.putExtras(this) }

                // 如果是模块Activity, 则对其替换, 否则不处理
                if (isModuleActivity(originIntent)) {
                    args[intentIndex] = safeIntent
                    KLogCat.tagI(
                        TAG,
                        arrayOf(
                            "替换成功!",
                            "originIntent: $originIntent",
                            "safeIntent: $safeIntent",
                        ),
                    )
                }

                // 2.替换到原来的Intent,欺骗AMS
                // args[intentIndex] = safeIntent

                // 3.之后,再换回来,启动我们未在AndroidManifest.xml中配置的Activity
                // final H mH = new H();
                // hook Handler消息的处理,给Handler增加mCallback
            }
            // public abstract int android.app.IActivityManager.startActivity(android.app.IApplicationThread,java.lang.String,android.content.Intent,java.lang.String,android.os.IBinder,java.lang.String,int,int,android.app.ProfilerInfo,android.os.Bundle) throws android.os.RemoteException
            // public abstract int android.app.IActivityTaskManager.startActivity(whoThread, who.getBasePackageName(), intent,intent.resolveTypeIfNeeded(who.getContentResolver()),token, target != null ? target.mEmbeddedID : null,requestCode, 0, null, options);
            return method.invoke(mIActivityManager, *(args ?: arrayOfNulls<Any>(0)))
        }
    }


    ////////

    /**
     * 启动未注册的Activity,将之前替换了的Intent,换回去.我们的目标是要启动未注册的Activity
     */
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    fun hookLauncherActivity() {
        try {
            // 1.获取ActivityThread的Class对象
            // package android.app
            // public final class ActivityThread
            val activityThreadClazz = Class.forName("android.app.ActivityThread")

            // 2.获取currentActivityThread()静态方法;为了保证在多个版本中兼容性,使用该静态方法获取ActivityThread的实例
            // public static ActivityThread currentActivityThread(){return sCurrentActivityThread;}
            val currentActivityThreadMethod =
                activityThreadClazz.getDeclaredMethod("currentActivityThread").also { it.isAccessible = true }

            // 3.获取ActivityThread的对象实例
            // public static ActivityThread currentActivityThread(){return sCurrentActivityThread;}
            val activityThreadObj = currentActivityThreadMethod.invoke(null)

            // 4.获取ActivityThread 的属性mH
            // final H mH = new H();
            val mHField = activityThreadClazz.getDeclaredField("mH").also { it.isAccessible = true }

            // 5.获取mH的值,既获取ActivityThread类中H类的实例对象
            // 从ActivityThread实例中获取mH属性对应的值,既mH的值
            val mHObj = mHField[activityThreadObj]

            // 6.获取Handler的Class对象
            // package android.os
            // public class Handler
            val handlerClazz = Class.forName("android.os.Handler")

            // 7.获取mCallback属性
            // final Callback mCallback;
            // Callback是Handler类内部的一个接口
            val mCallbackField = handlerClazz.getDeclaredField("mCallback").also { it.isAccessible = true }

            // 8.给mH增加mCallback
            // 给mH,既Handler的子类设置mCallback属性,提前对消息进行处理.
            // if (Build.VERSION.SDK_INT >= 28) {
            //     // >=android 9.0
            //     mCallbackField[mHObj] = HandlerCallbackP()
            // } else {
            //     mCallbackField[mHObj] = HandlerCallback()
            // }

            // >=android 9.0
            val originCallback = mCallbackField[mHObj] as Handler.Callback?
            mCallbackField[mHObj] = HandlerCallbackP(originCallback)

        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    /**
     * 从代理Intent替换到插件Intent
     *
     * [android9 ... android13]
     * 对大于Android 9.0版本的处理
     * https://www.cnblogs.com/Jax/p/9521305.html
     */
    private class HandlerCallbackP(private val originCallback: Handler.Callback?) : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            // android.app.ActivityThread$H.EXECUTE_TRANSACTION = 159
            // android 9.0反射,Accessing hidden field Landroid/app/ActivityThread$H;->EXECUTE_TRANSACTION:I (dark greylist, reflection)
            // android9.0 深灰名单（dark greylist）则debug版本在会弹出dialog提示框，在release版本会有Toast提示，均提示为"Detected problems with API compatibility"
            if (msg.what == 159) { // 直接写死,不反射了,否则在android9.0的设备上运行会弹出使用了反射的dialog提示框
                handleActivity(msg)
            }
            return false
        }

        @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
        private fun handleActivity(msg: Message) {
            try {
                // ClientTransaction-->ClientTransaction中的List<ClientTransactionItem> mActivityCallbacks-->集合中的第一个值LaunchActivityItem-->LaunchActivityItem的mIntent
                // 这里简单起见,直接取出TargetActivity;
                // final ClientTransaction transaction = (ClientTransaction) msg.obj;
                // 1.获取ClientTransaction对象
                val clientTransactionObj = msg.obj ?: return

                // filter
                // public ActivityLifecycleItem getLifecycleStateRequest() { return mLifecycleStateRequest; }
                val activityLifecycleItem =
                    Class.forName("android.app.servertransaction.ClientTransaction").getDeclaredMethod("getLifecycleStateRequest")
                        .also { it.isAccessible = true }.invoke(clientTransactionObj)

                val resumeActivityItemClazz = Class.forName("android.app.servertransaction.ResumeActivityItem")
                if (!resumeActivityItemClazz.isInstance(activityLifecycleItem)) return

                // 2.获取ClientTransaction类中属性mActivityCallbacks的Field
                // 3.禁止Java访问检查
                // private List<ClientTransactionItem> mActivityCallbacks;
                val mActivityCallbacksField =
                    clientTransactionObj.javaClass.getDeclaredField("mActivityCallbacks").also { it.isAccessible = true }

                // 4.获取ClientTransaction类中mActivityCallbacks属性的值,既List<ClientTransactionItem>
                val mActivityCallbacks = mActivityCallbacksField[clientTransactionObj] as? List<*>
                if (mActivityCallbacks == null || mActivityCallbacks.isEmpty()) return
                if (mActivityCallbacks[0] == null) return

                // 5.ClientTransactionItem的Class对象
                // package android.app.servertransaction;
                // public class LaunchActivityItem extends ClientTransactionItem
                val launchActivityItemClazz = Class.forName("android.app.servertransaction.LaunchActivityItem")

                // 6.判断集合中第一个元素的值是LaunchActivityItem类型的
                if (!launchActivityItemClazz.isInstance(mActivityCallbacks[0])) return

                // 7.获取LaunchActivityItem的实例
                // public class LaunchActivityItem extends ClientTransactionItem
                val launchActivityItem = mActivityCallbacks[0]

                // 8.ClientTransactionItem的mIntent属性的mIntent的Field
                // private Intent mIntent;
                // 9.禁止Java访问检查
                val mIntentField = launchActivityItemClazz.getDeclaredField("mIntent").also { it.isAccessible = true }

                // 10.获取mIntent属性的值,既桩Intent(安全的Intent)
                // 从LaunchActivityItem中获取属性mIntent的值
                val safeIntent = mIntentField[launchActivityItem] as? Intent ?: return

                // 11.获取原始的Intent
                // 12.需要判断originIntent != null
                val originIntent: Intent? = if (Build.VERSION.SDK_INT >= 33) {
                    safeIntent.getParcelableExtra(XPLER_ORIGIN_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    safeIntent.getParcelableExtra(XPLER_ORIGIN_INTENT)
                }
                if (originIntent == null) return


                // 如果不是模块Activity, 则对原方法消息事件进行回调, 并结束逻辑
                if (!isModuleActivity(originIntent)) {
                    originCallback?.handleMessage(msg)
                    return
                }

                // 13.将原始的Intent,赋值给clientTransactionItem的mIntent属性
                safeIntent.component = originIntent.component

                KLogCat.tagI(
                    TAG,
                    arrayOf(
                        "还原成功!",
                        "originIntent: $originIntent",
                        "safeIntent: $safeIntent",
                    ),
                )

                // 给插件apk设置主题
                val activityInfo: ActivityInfo = launchActivityItemClazz.getDeclaredField("mInfo")
                    .also { it.isAccessible = true }.get(launchActivityItem) as ActivityInfo
                activityInfo.theme = selectSystemTheme()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun selectSystemTheme(): Int {
        val targetSdkVersion: Int = Build.VERSION.SDK_INT
        val theme: Int = when {
            targetSdkVersion < 24 -> com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
            else -> com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        }
        return theme
    }

    //////

    /**
     * 动态代理 Instrumentation
     */
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private fun hookInstrumentation(
        application: Application,
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
            val mInstrumentationField = activityThreadClazz.getDeclaredField("mInstrumentation").also { it.isAccessible = true }
            val mInstrumentationObj = mInstrumentationField.get(activityThreadObj) as Instrumentation

            // 4.reset set value
            mInstrumentationField.set(activityThreadObj, PluginInstrumentationV2(mInstrumentationObj))
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }


    /**
     * 模块类判断
     */
    private fun isModuleActivity(intent: Intent): Boolean {
        // 这里对模块Activity进行判断
        val pluginActivityClassName = intent.component?.className ?: return false

        // 未获取到类加载器
        val classLoader = moduleClassloader ?: PluginBridgeV2.classLoader ?: return false

        // 是否模块Activity,
        return try {
            val pluginActivityClazz = classLoader.loadClass(pluginActivityClassName) ?: return false
            IPluginActivity::class.java.isAssignableFrom(pluginActivityClazz)
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}