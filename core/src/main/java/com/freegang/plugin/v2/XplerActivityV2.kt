package com.freegang.plugin.v2

import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import com.freegang.ktutils.extension.asOrNull
import com.freegang.plugin.PluginClassloader
import com.freegang.plugin.PluginContextThemeWrapper
import com.freegang.plugin.PluginResource
import com.freegang.plugin.base.BaseXplerActivityV2

open class XplerActivityV2 : BaseXplerActivityV2() {

    private val mClassLoader: PluginClassloader? = null

    private var mResources: PluginResource? = null

    private val content = mutableStateOf<(@Composable () -> Unit)?>(null)

    override fun getClassLoader(): ClassLoader {
        return mClassLoader ?: super.getClassLoader()
    }

    override fun getResources(): Resources {
        return mResources ?: super.getResources()
    }

    override fun getAssets(): AssetManager = mResources?.assets ?: super.getAssets()

    protected val pluginAssets: AssetManager get() = mResources?.pluginAssets ?: super.getAssets()

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