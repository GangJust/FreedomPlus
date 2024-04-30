package io.github.fplus.core.helper

import android.app.Application
import android.content.Intent
import android.os.CountDownTimer
import android.os.Process
import com.freegang.ktutils.app.KActivityUtils
import com.freegang.ktutils.app.KAppUtils
import io.github.fplus.core.hook.DouYinMain.Companion.freeExitHelper
import kotlin.system.exitProcess

class TimerExitHelper @JvmOverloads constructor(
    app: Application,
    millis: Long,
    private var keepBackground: Boolean = true,
    private var onTick: OnTickListener? = null
) {
    private var app: Application? = app
    private var initMillis: Long = millis
    private var remainingMillis: Long = millis
    private var countDownTimer: CountDownTimer? = null

    private var mIsStarted = false
    private var mIsPaused = false

    init {
        this.countDownTimer = createCountDownTimer(millis)
    }

    /**
     * 开始定时退出
     */
    fun start() {
        if (mIsStarted) {
            return
        }

        mIsStarted = true
        countDownTimer?.start()
    }

    /**
     * 重启定时退出
     */
    fun restart() {
        countDownTimer?.cancel()
        countDownTimer = createCountDownTimer(initMillis)
        mIsStarted = true
        mIsPaused = false
        countDownTimer?.start()
    }

    /**
     * 暂停定时退出
     */
    fun pause() {
        if (mIsPaused) {
            return
        }

        mIsPaused = true
        countDownTimer?.cancel()
        countDownTimer = createCountDownTimer(remainingMillis)
    }

    /**
     * 恢复定时退出
     */
    fun resume() {
        if (!mIsPaused) {
            return
        }

        mIsPaused = false
        countDownTimer?.start()
    }

    /**
     * 取消定时退出
     */
    fun cancel() {
        if (!mIsStarted) {
            return
        }

        countDownTimer?.cancel()
    }

    /**
     * 是否已经开始
     */
    val isStarted: Boolean
        get() = mIsStarted

    /**
     * 是否处于暂停状态
     */
    val isPaused: Boolean
        get() = mIsPaused

    /**
     * 创建定时退出实例
     * @param millis Long 定时退出时间
     */
    private fun createCountDownTimer(millis: Long): CountDownTimer {
        return object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                this@TimerExitHelper.remainingMillis = millisUntilFinished
                this@TimerExitHelper.onTick?.onTick(millisUntilFinished)

                if (!KAppUtils.isAppInForeground(app!!)) {
                    this@TimerExitHelper.pause()
                }
            }

            override fun onFinish() {
                keepOrKill()
            }
        }
    }

    /**
     * 保持后台运行或者杀死进程
     */
    private fun keepOrKill() {
        if (keepBackground) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addCategory(Intent.CATEGORY_HOME)
            app!!.startActivity(intent)
        } else {
            KActivityUtils.getActivities().forEach { it.finishAndRemoveTask() }
            Process.killProcess(Process.myPid())
            exitProcess(1)
        }
    }

    fun interface OnTickListener {
        fun onTick(millisUntilFinished: Long)
    }
}