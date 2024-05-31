package io.github.fplus.core.hook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freegang.extension.asOrNull
import com.freegang.extension.fieldGets
import com.freegang.extension.removeInParent
import com.ss.android.ugc.aweme.comment.constants.CommentColorMode
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.entity.ReturnType
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HCommentListPageFragment : BaseHook() {
    companion object {
        const val TAG = "HCommentListPageFragment"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.commentListPageFragmentClazz ?: NoneHook::class.java
    }

    @OnAfter
    @ReturnType(name = "com.ss.android.ugc.aweme.comment.constants.CommentColorMode")
    fun changeCommentColorModeAfter(params: XC_MethodHook.MethodHookParam/*, mode: CommentColorMode?*/) {
        hookBlockRunning(params) {
            if (!config.isCommentColorMode) return

            result = when (config.commentColorMode) {
                0 -> {
                    CommentColorMode.MODE_LIGHT
                }

                1 -> {
                    CommentColorMode.MODE_DARK
                }

                else -> {
                    CommentColorMode.MODE_LIGHT_OR_DARK
                }
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter("onCreateView")
    fun onCreateViewAfter(
        params: XC_MethodHook.MethodHookParam,
        inflater: LayoutInflater,
        parent: ViewGroup?,
        bundle: Bundle?,
    ) {
        hookBlockRunning(params) {
            val view = thisObject.fieldGets(type = View::class.java)
                .asOrNull<List<View?>>()
                ?.firstOrNull { "${it?.contentDescription}".startsWith("搜索，") }

            view?.removeInParent()
        }.onFailure {
            XplerLog.e(it)
        }
    }
}