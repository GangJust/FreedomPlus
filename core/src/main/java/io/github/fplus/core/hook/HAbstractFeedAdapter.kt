package io.github.fplus.core.hook

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.core.view.updatePadding
import com.freegang.ktutils.display.dip2px
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.onEachChild
import com.ss.android.ugc.aweme.ad.feed.VideoViewHolderRootView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.NoneHook
import io.github.xpler.core.OnAfter
import io.github.xpler.core.Param
import io.github.xpler.core.hookBlockRunning

class HAbstractFeedAdapter(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam) {
    companion object {
        const val TAG = "HAbstractFeedAdapter"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.abstractFeedAdapterClazz ?: NoneHook::class.java
    }

    @OnAfter
    fun methodAfter(
        params: XC_MethodHook.MethodHookParam,
        @Param("null") any: Any?,
        view: View?,
        i: Int,
    ) {
        // 垫高?
        hookBlockRunning(params) {
            if (config.isImmersive) {
                // KLogCat.d("view: $view")
                view ?: return
                if (view is FrameLayout && view !is VideoViewHolderRootView) {
                    view.onEachChild { if (background is GradientDrawable) background = null }
                    val bottomPadding = view.context.dip2px(58f) // BottomTabBarHeight
                    val viewGroup = view.children.last().asOrNull<ViewGroup>() ?: return
                    viewGroup.updatePadding(bottom = bottomPadding)
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}