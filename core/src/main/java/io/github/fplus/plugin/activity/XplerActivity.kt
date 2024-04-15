package io.github.fplus.plugin.activity

import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.freegang.extension.asOrNull
import io.github.fplus.plugin.PluginClassloader
import io.github.fplus.plugin.PluginContextThemeWrapper
import io.github.fplus.plugin.PluginResources
import io.github.fplus.plugin.base.BaseActivity

abstract class XplerActivity : BaseActivity() {

    private val mClassLoader by lazy { PluginClassloader() }

    private var mResources: Resources? = null

    private val content = mutableStateOf<(@Composable () -> Unit)?>(null)

    override fun getClassLoader(): ClassLoader = mClassLoader

    override fun getResources(): Resources {
        return mResources ?: super.getResources()
    }

    override fun getAssets(): AssetManager {
        val resources = mResources
        if (resources is PluginResources?) {
            return resources?.pluginAssets ?: mResources?.assets ?: super.getAssets()
        }
        return mResources?.assets ?: super.getAssets()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //
        actionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        //
        val wrapper = PluginContextThemeWrapper(this)
        setContentView(ComposeView(wrapper).apply {
            setContent {
                mResources = LocalContext.current.resources?.asOrNull() // 强转
                content.value?.invoke()
            }
        })
    }

    protected fun setContent(content: @Composable () -> Unit) {
        this.content.value = content
    }
}