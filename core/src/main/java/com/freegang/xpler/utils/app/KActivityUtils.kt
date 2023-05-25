package com.freegang.xpler.utils.app

import android.annotation.SuppressLint
import android.app.Activity
import android.view.ViewGroup
import android.view.Window

object KActivityUtils {

    /**
     * 反射获取当前顶部活跃的Activity
     * @return Activity
     */
    fun getTopActivity(): Activity? {
        return try {
            val activities = getActivities()
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

    /**
     * 反射获取当前的所有Activity
     * @return List<Activity>
     */
    fun getActivityList(): List<Activity> {
        val activities = getActivities()
        val activityList = mutableListOf<Activity>()
        for (activityRecord in activities.values) {
            val activityRecordClass = activityRecord!!.javaClass
            val activityField = activityRecordClass.getDeclaredField("activity")
            activityField.isAccessible = true

            val activity = activityField.get(activityRecord) as Activity
            activityList.add(activity)
        }

        return activityList
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    private fun getActivities(): Map<*, *> {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread")
        currentActivityThreadMethod.isAccessible = true
        val currentActivityThread = currentActivityThreadMethod.invoke(null)

        val activitiesField = activityThreadClass.getDeclaredField("mActivities")
        activitiesField.isAccessible = true
        return activitiesField.get(currentActivityThread) as Map<*, *>
    }
}

val Any.topActivity get() = KActivityUtils.getTopActivity()

val Activity.contentView get() = this.window.decorView.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)

val Window.contentView get() = this.decorView.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)