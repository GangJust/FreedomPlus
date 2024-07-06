package io.github.fplus.plugin.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import io.github.fplus.plugin.PluginContextThemeWrapper
import io.github.fplus.plugin.base.BaseXplerDialog

abstract class XplerDialog(context: Context) : BaseXplerDialog(context) {
    private val content = mutableStateOf<(@Composable () -> Unit)?>(null)

    init {
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
            WindowCompat.setDecorFitsSystemWindows(this, false)
            // WindowCompat.getInsetsController(this, decorView).hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*runCatching {
            KLogCat.d("插件资源: ${context.resources.getString(R.string.app_name)}")
        }.onFailure {
            KLogCat.d("获取异常: \n\n${it.stackTraceToString()}")
        }*/

        val wrapper = PluginContextThemeWrapper(context)
        setContentView(ComposeView(wrapper).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )

            setContent {
                content.value?.invoke()
            }
        })
    }

    fun setContent(content: @Composable () -> Unit) {
        this.content.value = content
    }
}