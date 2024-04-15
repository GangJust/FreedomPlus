package io.github.fplus.plugin.activity

import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.freegang.extension.asOrNull
import io.github.fplus.plugin.PluginClassloader
import io.github.fplus.plugin.PluginContextThemeWrapper
import io.github.fplus.plugin.PluginResources
import io.github.fplus.plugin.base.BaseComponentActivity

abstract class XplerComponentActivity : BaseComponentActivity() {

    private val mClassLoader by lazy { PluginClassloader() }

    private var mResources: PluginResources? = null

    private val content = mutableStateOf<(@Composable () -> Unit)?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        // 方案一: 资源代理在Android 10上宿主无法找到 android.content.res.loader.ResourcesLoader (而这个错误是抖音内反射调用的报错, 猜测是Classloader出的问题)
        // 方案二: 资源合并在抖音启动时有一个 layout/_ 资源的报错崩溃。
        // 方案三: 方案一的基础上不对宿主资源进行代理, 插件只使用自身资源, 本质上都是创建新的资源, 能跑但不知道为什么, 每隔一段时间都会出现资源找不到的情况(而方案一却不会)。
        super.onCreate(savedInstanceState)

        //
        initViewTreeOwners()

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

    override fun getClassLoader(): ClassLoader = mClassLoader

    override fun getResources(): Resources = mResources ?: super.getResources()

    override fun getAssets(): AssetManager = mResources?.pluginAssets ?: mResources?.assets ?: super.getAssets()

    override fun getTheme(): Resources.Theme = mResources?.newTheme() ?: super.getTheme()

    private fun initViewTreeOwners() {
        window!!.decorView.setViewTreeLifecycleOwner(this)
        window!!.decorView.setViewTreeViewModelStoreOwner(this)
        window!!.decorView.setViewTreeSavedStateRegistryOwner(this)
        window!!.decorView.setViewTreeOnBackPressedDispatcherOwner(this)
    }
}