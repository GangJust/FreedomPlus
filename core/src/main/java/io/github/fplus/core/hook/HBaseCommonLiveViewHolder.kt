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
import com.freegang.ktutils.reflect.fieldGets
import com.freegang.ktutils.view.traverse
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.xpler.core.OnAfter
import io.github.xpler.core.hookBlockRunning

class HBaseCommonLiveViewHolder(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam) {
    companion object {
        const val TAG = "HBaseCommonLiveViewHolder"
    }

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.viewholder.BaseCommonLiveViewHolder")
    }

    @OnAfter("bind")
    fun bindAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            val view = thisObject.fieldGets(type = View::class.java)
                .firstOrNull()

            // KLogCat.d("view: $view")
            if (view is FrameLayout) {
                view.traverse { if (background is GradientDrawable) background = null }
                val bottomPadding = view.context.dip2px(58f) // BottomTabBarHeight
                val viewGroup = view.children.last().asOrNull<ViewGroup>() ?: return
                viewGroup.updatePadding(bottom = bottomPadding)
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}