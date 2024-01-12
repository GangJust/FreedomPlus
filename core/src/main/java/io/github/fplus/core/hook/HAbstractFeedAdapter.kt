package io.github.fplus.core.hook

import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.updatePadding
import com.freegang.ktutils.display.dip2px
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.firstOrNull
import com.freegang.ktutils.view.firstParentOrNull
import com.freegang.ktutils.view.forEachChild
import com.ss.android.ugc.aweme.ad.feed.VideoViewHolderRootView
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.hookBlockRunning
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay

class HAbstractFeedAdapter : BaseHook<Any>() {
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
        // @Param("com.ss.android.ugc.aweme.feed.adapter.FeedTypeConfig") feedTypeConfig: Any?,
        view: View?,
        i: Int,
    ) {
        hookBlockRunning(params) {
            if (!config.isImmersive) return
            // KLogCat.d("view: $view")
            if (view is FrameLayout && view !is VideoViewHolderRootView) {

                // 垫高
                view.forEachChild { if (background is GradientDrawable) background = null }
                val bottomPadding = view.context.dip2px(58f) // BottomTabBarHeight
                val viewGroup = view.children.last().asOrNull<ViewGroup>() ?: return
                viewGroup.updatePadding(bottom = bottomPadding)

                launch {
                    delay(300)

                    // 尝试修复直播控件漂移
                    val orNull = view.firstOrNull<View> { it.javaClass.name.endsWith("AutoEnterProgressBar") }
                    val orNull1 = orNull?.firstParentOrNull(LinearLayout::class.java)
                    orNull1?.gravity = Gravity.CENTER_HORIZONTAL

                    cancel()
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}