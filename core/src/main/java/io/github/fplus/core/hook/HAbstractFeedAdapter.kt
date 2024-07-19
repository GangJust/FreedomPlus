package io.github.fplus.core.hook

import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.freegang.extension.dip2px
import com.freegang.extension.firstOrNull
import com.freegang.extension.firstParentOrNull
import com.freegang.extension.forEachChild
import com.ss.android.ugc.aweme.ad.feed.VideoViewHolderRootView
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import kotlinx.coroutines.delay

class HAbstractFeedAdapter : BaseHook() {
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
            if (!config.isImmersive)
                return

            // KLogCat.d("view: $view")
            if (view is FrameLayout && view !is VideoViewHolderRootView) {

                // 垫高
                view.forEachChild { if (it.background is GradientDrawable) it.background = null }
                val bottomPadding = 58f.dip2px() // BottomTabBarHeight
                val viewGroup = view.children.lastOrNull { it is ViewGroup && it.isVisible } ?: return
                viewGroup.updatePadding(bottom = bottomPadding)

                singleLaunchMain {
                    delay(300)

                    // 尝试修复直播控件漂移
                    val orNull = view.firstOrNull(View::class.java) { it.javaClass.name.endsWith("AutoEnterProgressBar") }
                    val orNull1 = orNull?.firstParentOrNull(LinearLayout::class.java)
                    orNull1?.gravity = Gravity.CENTER_HORIZONTAL
                }
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}