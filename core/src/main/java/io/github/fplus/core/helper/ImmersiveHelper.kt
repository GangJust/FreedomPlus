package io.github.fplus.core.helper

import android.app.Activity
import android.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.freegang.ktutils.app.navBarInteractionMode

object ImmersiveHelper {
    @get:Synchronized
    @set:Synchronized
    var isEdgeToEdgeEnabled = false

    fun immersive(activity: Activity) {
        val window = activity.window
        if (activity.navBarInteractionMode == 2) {
            ImmersiveHelper.isEdgeToEdgeEnabled = true
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        } else {
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.parseColor("#161616")
        }
    }

    fun statusBar(activity: Activity, isHide: Boolean) {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).apply {
            if (isHide) {
                hide(WindowInsetsCompat.Type.statusBars())
            } else {
                show(WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    fun navigationBars(activity: Activity, isHide: Boolean) {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).apply {
            if (isHide) {
                hide(WindowInsetsCompat.Type.navigationBars())
            } else {
                show(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }
}