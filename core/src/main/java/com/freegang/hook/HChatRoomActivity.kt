package com.freegang.hook

import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.freegang.base.BaseHook
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.view.parentView
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.thisActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HChatRoomActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<Any>(lpparam) {
    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.im.sdk.chat.ChatRoomActivity")
    }

    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    @OnAfter("onCreate")
    fun onCreateAfter(params: XC_MethodHook.MethodHookParam, bundle: Bundle?) {
        hookBlock(params) {
            val contentView = thisActivity.contentView
            onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
                contentView.traverse {
                    if ("${it.contentDescription}".contains("邀请朋友")) {
                        it.parent.parentView.isVisible = false
                    }
                }
            }
            contentView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
        }
    }

    @OnBefore("onDestroy")
    fun onDestroyBefore(params: XC_MethodHook.MethodHookParam) {
        hookBlock(params) {
            thisActivity.contentView.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
            onGlobalLayoutListener = null
        }
    }
}