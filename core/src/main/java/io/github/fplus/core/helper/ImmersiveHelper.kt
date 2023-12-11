package io.github.fplus.core.helper

import android.app.Activity
import android.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

object ImmersiveHelper {
    fun immersive(
        activity: Activity,
        hideStatusBar: Boolean = false,
        hideNavigationBars: Boolean = false,
    ) {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
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
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }
}