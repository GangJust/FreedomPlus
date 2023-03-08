package com.freegang.xpler.utils.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object KNotifiUtils {

    /**
     * 显示普通消息通知
     */
    @JvmStatic
    @JvmOverloads
    fun showNotification(
        context: Context,
        notifyId: Int,
        channelId: String = "渠道ID",
        channelName: String = "渠道名",
        title: String,
        text: String,
        intent: PendingIntent? = null,
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT, //默认, 酌情修改
            )
            manager.createNotificationChannel(notificationChannel)
        }

        val notify = NotificationCompat.Builder(context, channelId).apply {
            setAutoCancel(true) //自动取消
            setSmallIcon(context.applicationInfo.icon)
            setContentTitle(title)
            setContentText(text)
            if (intent != null) {
                setContentIntent(intent)
            }
        }

        manager.notify(notifyId, notify.build())
    }

    /**
     * 显示进度条通知
     */
    @JvmStatic
    @JvmOverloads
    fun showProgressNotification(
        context: Context,
        notifyId: Int,
        channelId: String = "渠道ID",
        channelName: String = "渠道名",
        title: String = "正在下载..",
        inProgressText: String = "下载中%s%%",
        finishedText: String = "下载完成!",
        intent: PendingIntent? = null,
        listener: KNotifiUtils.ProgressNotificationListener,
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT //默认, 酌情修改
            )
            manager.createNotificationChannel(notificationChannel)
        }

        //构建通知
        val notify = NotificationCompat.Builder(context, channelId).apply {
            setAutoCancel(true) //自动取消
            setSmallIcon(context.applicationInfo.icon)
            setContentTitle(title)
            setContentText(inProgressText.format(0))
            setProgress(100, 0, false)
            //notify.setProgress(0, 0, true) //不确定状态
            if (intent != null) {
                setContentIntent(intent)
            }
        }

        //回调,由调用者设置进度
        listener.on(ProgressNotification(notifyId, inProgressText, finishedText, manager, notify))
    }


    //kotlin
    fun showProgressNotification(
        context: Context,
        notifyId: Int,
        channelId: String = "渠道ID",
        channelName: String = "渠道名",
        title: String = "正在下载..",
        inProgressText: String = "下载中%s%%",
        finishedText: String = "下载完成!",
        intent: PendingIntent? = null,
        listener: (notify: KNotifiUtils.ProgressNotification) -> Unit,
    ) {
        KNotifiUtils.showProgressNotification(
            context = context,
            notifyId = notifyId,
            channelId = channelId,
            channelName = channelName,
            title = title,
            inProgressText = inProgressText,
            finishedText = finishedText,
            intent = intent,
            listener = object : ProgressNotificationListener {
                override fun on(notify: ProgressNotification) {
                    listener.invoke(notify)
                }
            },
        )
    }


    //控制类
    class ProgressNotification(
        private val notifyId: Int = 1,
        private var inProgressText: String,
        private var finishedText: String,
        private val manager: NotificationManager,
        private val notify: NotificationCompat.Builder,
    ) {
        /**
         * 由调用者主动设置完成文本
         * @param finishedText 下载完成后展示的文本, 默认为: "下载完成!"
         */
        @JvmOverloads
        fun setFinishedText(
            finishedText: String = "下载完成!",
        ) {
            this.finishedText = finishedText
            notify.setProgress(100, 100, false)
            notify.setContentText(this.finishedText)
            manager.notify(notifyId, notify.build())
        }

        /**
         * 调用者设置进度, 应该由它实时更新
         * @param step 当前进度
         * @param inProgressText 进度文本, 应该预留一个`%s`或者`%d`作为[step]的展示, 默认为: "下载中%s%%"
         */
        @JvmOverloads
        fun notifyProgress(
            step: Int,
            inProgressText: String = "下载中%s%%",
        ) {
            this.inProgressText = inProgressText
            notify.setContentText(this.inProgressText.format(step))
            notify.setProgress(100, step, false)
            manager.notify(notifyId, notify.build())
        }
    }

    @FunctionalInterface
    interface ProgressNotificationListener {
        fun on(notify: KNotifiUtils.ProgressNotification)
    }
}
