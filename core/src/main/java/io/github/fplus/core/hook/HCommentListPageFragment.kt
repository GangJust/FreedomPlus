package io.github.fplus.core.hook

import android.os.Bundle
import android.view.View
import com.freegang.extension.forEachWhereChild
import com.freegang.extension.removeInParent
import com.ss.android.ugc.aweme.comment.constants.CommentColorMode
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.HookEntity
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.entity.OnBefore
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

    override fun onInit() {
        HCommentColorModeViewMode()
    }

    @OnAfter("onViewCreated")
    fun onViewCreatedAfter(
        params: XC_MethodHook.MethodHookParam,
        view: View,
        savedInstanceState: Bundle?,
    ) {
        hookBlockRunning(params) {
            view.forEachWhereChild {
                if (it.contentDescription?.toString()?.startsWith("搜索，") == true) {
                    it.removeInParent()
                    return@forEachWhereChild true
                }

                false
            }
        }.onFailure {
            XplerLog.tagE(TAG, it)
        }
    }

    // 评论颜色模式
    private inner class HCommentColorModeViewMode : HookEntity() {
        override fun setTargetClass(): Class<*> {
            return DexkitBuilder.commentColorModeViewModeClazz ?: NoneHook::class.java
        }

        @OnBefore
        fun setCommentColorModeBefore(
            params: XC_MethodHook.MethodHookParam,
            mode: CommentColorMode,
        ) {
            hookBlockRunning(params) {
                if (!config.isCommentColorMode)
                    return

                args[0] = when (config.commentColorMode) {
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
                XplerLog.tagE(TAG, it)
            }
        }
    }
}