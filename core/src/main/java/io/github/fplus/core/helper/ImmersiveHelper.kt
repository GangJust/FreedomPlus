package io.github.fplus.core.helper

import android.app.Activity
import android.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import io.github.fplus.core.config.ConfigV1
import com.freegang.ktutils.app.navBarInteractionMode

object ImmersiveHelper {
    @get:Synchronized
    @set:Synchronized
    var isEdgeToEdgeEnabled = false

    fun with(activity: Activity, config: ConfigV1) {
        // 全屏沉浸式
        if (config.isImmersive) {
            val window = activity.window
            if (config.systemControllerValue[0]) {
                WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.statusBars())
            }
            if (config.systemControllerValue[1]) {
                WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.navigationBars())
            }
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
    }
}