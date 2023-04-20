package com.freegang.douyin.logic

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.freegang.base.BaseHook

class ClipboardLogic(
    private val hook: BaseHook<*>,
) {
    private var primaryClipChangedListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    // 添加剪贴板监听
    fun addClipboardListener(context: Context, notify: (clipData: ClipData?) -> Unit) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        primaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
            val clipData = clipboardManager.primaryClip
            if (!clipboardManager.hasPrimaryClip() || clipData!!.itemCount <= 0) return@OnPrimaryClipChangedListener

            //获取剪贴板内容
            val clipDataItem = clipData.getItemAt(0)
            val shareText = clipDataItem.text.toString()
            if (!shareText.contains("http")) return@OnPrimaryClipChangedListener

            //跳过直播链接, 按文本检查
            if (shareText.contains("【抖音】") && shareText.contains("正在直播") && shareText.contains("一起支持")) {
                hook.showToast(context, "不支持直播!")
                return@OnPrimaryClipChangedListener
            }
            notify.invoke(clipData)
        }
        clipboardManager.addPrimaryClipChangedListener(primaryClipChangedListener)
    }

    // 移除剪贴板监听
    fun removeClipboardListener(context: Context) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.removePrimaryClipChangedListener(primaryClipChangedListener)
    }
}