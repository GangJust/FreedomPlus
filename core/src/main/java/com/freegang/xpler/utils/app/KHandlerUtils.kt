package com.freegang.xpler.utils.app

import android.os.Handler
import android.os.Looper

object KHandlerUtils {
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 在主线程中执行任务
     * @param task 要执行的任务
     */
    @JvmStatic
    fun runOnUiThread(task: Runnable) {
        if (isMainThread()) {
            task.run()
        } else {
            mainHandler.post(task)
        }
    }

    /**
     * 在指定延迟后在主线程中执行任务
     * @param delayMillis 延迟的时间（毫秒）
     * @param task 要执行的任务
     */
    @JvmStatic
    fun runOnUiThreadDelayed(delayMillis: Long, task: Runnable) {
        mainHandler.postDelayed(task, delayMillis)
    }

    /**
     * 取消在主线程中延迟执行的任务
     * @param task 要取消的任务
     */
    @JvmStatic
    fun cancelOnUiThreadDelayed(task: Runnable) {
        mainHandler.removeCallbacks(task)
    }

    /**
     * 判断当前线程是否为主线程
     * @return 当前线程是否为主线程
     */
    @JvmStatic
    fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    /**
     * 创建一个延迟执行的任务
     */
    @JvmStatic
    fun createDelayedTask(delayMillis: Long, task: Runnable): Runnable {
        return Runnable { runOnUiThreadDelayed(delayMillis, task) }
    }
}