package io.github.fplus.core.helper

import android.app.Activity
import android.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.freegang.ktutils.app.navBarInteractionMode

object ImmersiveHelper {
    fun immersive(
        activity: Activity,
        hideStatusBar: Boolean = false,
        hideNavigationBars: Boolean = false,
    ) {
        var finalHideStatusBar = hideStatusBar
        var finalHideNavigationBars = hideNavigationBars

        // 底部三键导航
        if (activity.navBarInteractionMode == 0) {
            finalHideStatusBar = true
            finalHideNavigationBars = true
        }

        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (finalHideStatusBar) {
                hide(WindowInsetsCompat.Type.statusBars())
            } else {
                show(WindowInsetsCompat.Type.statusBars())
            }

            if (finalHideNavigationBars) {
                hide(WindowInsetsCompat.Type.navigationBars())
            } else {
                show(WindowInsetsCompat.Type.navigationBars())
            }
        }
        transparentBar(activity)
    }

    fun transparentBar(
        activity: Activity,
    ) {
        val window = activity.window
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }
}