package io.github.fplus.core.helper

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object ImmersiveHelper {
    fun immersive(
        activity: Activity,
        hideStatusBar: Boolean = false,
        hideNavigationBars: Boolean = false,
    ) {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (hideStatusBar) {
                hide(WindowInsetsCompat.Type.statusBars())
            } else {
                show(WindowInsetsCompat.Type.statusBars())
            }

            if (hideNavigationBars) {
                hide(WindowInsetsCompat.Type.navigationBars())
            } else {
                show(WindowInsetsCompat.Type.navigationBars())
            }
        }

        // 异形屏处理，允许页面延伸到刘海区域
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
    }

    fun systemBarColor(
        activity: Activity,
        statusBarColor: Int? = Color.TRANSPARENT,
        navigationBarColor: Int? = Color.TRANSPARENT,
    ) {
        val window = activity.window
        if (statusBarColor != null) {
            window.statusBarColor = statusBarColor
        }
        if (navigationBarColor != null) {
            window.navigationBarColor = navigationBarColor
        }
    }
}