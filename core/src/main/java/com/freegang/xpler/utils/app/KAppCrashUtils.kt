package com.freegang.xpler.utils.app

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Looper
import android.os.Process
import android.widget.Toast
import com.freegang.xpler.utils.log.KLogCat
import kotlin.system.exitProcess

/// 全局未捕获异常工具
/// 请在 Application.onCrate() 中初始化
class KAppCrashUtils : Thread.UncaughtExceptionHandler {
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null

    private var mApp: Application? = null
    private var mErrActivity: Class<out Activity>? = null
    private var mMessage: String = ""

    companion object {
        @JvmStatic
        val instance: KAppCrashUtils = KAppCrashUtils()
    }

    fun init(app: Application, message: String = "程序崩溃!") {
        init(app, null, message)
    }

    @JvmOverloads
    fun init(app: Application, errActivity: Class<out Activity>? = null, message: String = "程序崩溃!") {
        this.mApp = app
        this.mErrActivity = errActivity
        this.mMessage = message
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        //设置KAppCrashUtils为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /// 全局异常处理方法
    override fun uncaughtException(t: Thread, e: Throwable) {
        if (!handlerException(e) && mDefaultHandler != null) {
            mDefaultHandler?.uncaughtException(t, e)
        } else {
            exitAppOrStartErrActivity(e)
        }
    }

    /// 异常处理
    private fun handlerException(e: Throwable): Boolean {
        Thread {
            Looper.prepare()
            Toast.makeText(mApp, mMessage, Toast.LENGTH_SHORT).show()
            Looper.loop()
        }.start()
        val errMessage = "\n发生错误: ${e.message}\n" +
                "出现时间: ${KLogCat.dateTimeFormat.format(System.currentTimeMillis())}\n" +
                "设备信息: ${Build.MANUFACTURER} ${Build.MODEL}\n" +
                "系统版本: Android ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})\n" +
                "应用版本: ${mApp!!.appLabelName} ${mApp!!.appVersionName} (${mApp!!.appVersionCode})\n" +
                "堆栈信息: ${e.stackTraceToString()}\n"
        KLogCat.e(errMessage)
        return true
    }

    /// 结束应用
    private fun exitAppOrStartErrActivity(e: Throwable) {
        if (mErrActivity != null) {
            val intent = Intent(mApp!!.applicationContext, mErrActivity)
            intent.putExtra("message", e.message)
            intent.putExtra("stack_trace", e.stackTraceToString())
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            mApp!!.startActivity(intent)
            Process.killProcess(Process.myPid())
            exitProcess(1)
        } else {
            //等待1秒toast显示
            try {
                Thread.sleep(1000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mApp!!.topActivity?.finishAffinity()
            Process.killProcess(Process.myPid())
            exitProcess(1)
        }
    }
}