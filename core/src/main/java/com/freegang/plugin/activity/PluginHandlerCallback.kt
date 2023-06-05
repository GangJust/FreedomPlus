package com.freegang.plugin.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.Log
import com.freegang.ktutils.reflect.findField

class PluginHandlerCallback : Handler.Callback {
    private val TAG = "PluginBridge"

    @SuppressLint("PrivateApi")
    override fun handleMessage(msg: Message): Boolean {
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
            mCallbackField.set(mH, object : Handler.Callback {
                override fun handleMessage(msg: Message): Boolean {
                    when (msg.what) {
                        100 -> { //Android 8- -> ActivityThread.H.LAUNCH_ACTIVITY = 100
                            val mIntentField = msg.obj::class.java.findField("intent")
                            val proxyIntent = mIntentField.get(msg.obj) as Intent? ?: return false
                            val original = proxyIntent.getParcelableExtra(PluginActivityBridge.ORIGINAL_INTENT_KEY) as Intent?
                                ?: return false
                            original.putExtra(PluginActivityBridge.ORIGINAL_INTENT_KEY, "OK")
                            mIntentField.set(msg.obj, original)  //替换回来, 对系统进行欺骗
                            Log.d(TAG, "handleMessage: 恢复成功!")
                        }

                        159 -> { // Android 8+ -> ActivityThread.H.EXECUTE_TRANSACTION = 159
                            val mActivityCallbacksField = msg.obj::class.java.findField("mActivityCallbacks")
                            val mActivityCallbacks = mActivityCallbacksField.get(msg.obj) as List<*>

                            val launchActivityItemClazz = Class.forName("android.app.servertransaction.LaunchActivityItem")
                            for (item in mActivityCallbacks) {
                                if (launchActivityItemClazz.isInstance(item)) {
                                    val mIntentField = launchActivityItemClazz.findField("mIntent")
                                    val proxyIntent = mIntentField.get(item) as Intent
                                    val original =
                                        proxyIntent.getParcelableExtra(PluginActivityBridge.ORIGINAL_INTENT_KEY) as Intent?
                                            ?: return false
                                    original.putExtra(PluginActivityBridge.ORIGINAL_INTENT_KEY, "OK")
                                    mIntentField.set(item, original)  //恢复回来, 对系统进行欺骗
                                    Log.d(TAG, "handleMessage: 恢复成功!")
                                }
                            }
                        }
                    }
                    return false
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}