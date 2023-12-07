package io.github.fplus.plugin.activity

import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import com.freegang.ktutils.extension.asOrNull
import io.github.fplus.plugin.PluginClassloader
import io.github.fplus.plugin.PluginContextThemeWrapper
import io.github.fplus.plugin.PluginResources
import io.github.fplus.plugin.base.BaseActivity

abstract class XplerActivity : BaseActivity() {

    private val mClassLoader: PluginClassloader? = null

    private var mResources: PluginResources? = null

    private val content = mutableStateOf<(@Composable () -> Unit)?>(null)

    override fun getClassLoader(): ClassLoader {
        return mClassLoader ?: super.getClassLoader()
    }

    override fun getResources(): Resources {
        return mResources ?: super.getResources()
    }

    override fun getAssets(): AssetManager = mResources?.pluginAssets ?: mResources?.assets ?: super.getAssets()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //
        actionBar?.hide()
        window.statusBarColor = android.graphics.Color.TRANSPARENT

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