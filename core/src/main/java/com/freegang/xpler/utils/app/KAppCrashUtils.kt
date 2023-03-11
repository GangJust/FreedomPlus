package com.freegang.xpler.utils.app

import android.app.Application
import android.os.Looper
import android.widget.Toast
import com.freegang.xpler.utils.log.KLogCat
import kotlin.system.exitProcess

/// 全局未捕获异常工具
/// 请在 Application.onCrate() 中初始化
class KAppCrashUtils : Thread.UncaughtExceptionHandler {
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null

    private var mApp: Application? = null
    private var mMessage: String = ""

    companion object {
        @JvmStatic
        val instance: KAppCrashUtils = KAppCrashUtils()
    }

    @JvmOverloads
    fun init(app: Application, message: String = "程序崩溃!") {
        this.mApp = app
        this.mMessage = message
        //初始化日志记录工具
        KLogCat.init(app)
        KLogCat.openStorage()
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该MyCrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /// 全局异常处理方法
    override fun uncaughtException(t: Thread, e: Throwable) {
        if (!handlerException(e) && mDefaultHandler != null) {
            mDefaultHandler?.uncaughtException(t, e);
        } else {
            try {
                //Sleep 来让线程停止一会是为了显示Toast信息给用户，然后Kill程序
                Thread.sleep(2000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            exitApp()
        }
    }

    /// 异常处理
    private fun handlerException(e: Throwable): Boolean {
        Thread {
            Looper.prepare()
            Toast.makeText(mApp, mMessage, Toast.LENGTH_SHORT).show()
            Looper.loop()
        }.start()

        //记录日志
        KLogCat.e("$mMessage\n${e.stackTraceToString()}")
        return true
    }

    /// 结束应用
    private fun exitApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        exitProcess(1);
    }
}