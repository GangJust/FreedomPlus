package com.freegang.xpler.utils.app

import android.annotation.SuppressLint
import android.app.Activity
import android.view.ViewGroup
import android.view.Window

object KActivityUtils {

    /**
     * 反射获取当顶部活跃的Activity
     * @return activity
     */
    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun getTopActivity(): Activity? {
        return try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread")
            currentActivityThreadMethod.isAccessible = true
            val activityThread = currentActivityThreadMethod.invoke(null)

            val activitiesField = activityThreadClass.getDeclaredField("mActivities")
            activitiesField.isAccessible = true
            val activities = activitiesField.get(activityThread) as Map<*, *>

            var topActivity: Activity? = null

            for (activityRecord in activities.values) {
                val activityRecordClass = activityRecord!!.javaClass
                val pausedField = activityRecordClass.getDeclaredField("paused")
                pausedField.isAccessible = true
                val isPaused = pausedField.getBoolean(activityRecord)

                if (!isPaused) {
                    val activityField = activityRecordClass.getDeclaredField("activity")
                    activityField.isAccessible = true
                    topActivity = activityField.get(activityRecord) as? Activity
                    break
                }
            }
            topActivity
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val Any.topActivity get() = getTopActivity()

    val Window.contentView: ViewGroup get() = this.decorView.findViewById(Window.ID_ANDROID_CONTENT)

    val Activity.contentView get() = this.window.contentView

}