package io.github.fplus.core.hook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGets
import com.freegang.ktutils.view.removeInParent
import com.ss.android.ugc.aweme.comment.constants.CommentColorMode
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.NoneHook
import io.github.xpler.core.OnAfter
import io.github.xpler.core.ReturnType
import io.github.xpler.core.hookBlockRunning

class HCommentListPageFragment(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam) {
    companion object {
        const val TAG = "HCommentListPageFragment"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.commentListPageFragmentClazz ?: NoneHook::class.java
    }

    @OnAfter
    @ReturnType(type = CommentColorMode::class)
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
            KLogCat.tagE(TAG, it)
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
            KLogCat.tagE(TAG, it)
        }
    }
}