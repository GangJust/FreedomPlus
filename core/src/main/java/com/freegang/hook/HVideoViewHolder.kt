package com.freegang.hook

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import de.robv.android.xposed.callbacks.XC_LoadPackage

@Deprecated("淘汰区域，删除倒计时中")
class HVideoViewHolder(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<Any>(lpparam) {
    companion object {
        const val TAG = "HVideoViewHolder"
    }

    private val config get() = ConfigV1.get()

    override fun setTargetClass(): Class<*> {
        return findClass("com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder")
    }
}