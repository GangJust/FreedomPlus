package io.github.fplus.core.hook.logic

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import io.github.fplus.core.base.BaseHook

class ClipboardLogic(
    private val hook: BaseHook,
) {
    private var primaryClipChangedListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    // 添加剪贴板监听
    fun addClipboardListener(context: Context, notify: (clipData: ClipData, firstText: String) -> Unit) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        primaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
            if (!clipboardManager.hasPrimaryClip())
                return@OnPrimaryClipChangedListener

            clipboardManager.primaryClip?.runCatching {
                // 获取剪贴板内容
                val clipDataItem = getItemAt(0)
                val shareText = "${clipDataItem.text}"

                if (!shareText.contains("http"))
                    return@OnPrimaryClipChangedListener

                // 跳过直播链接, 按文本检查
                if (shareText.contains("【抖音】") && shareText.contains("正在直播") && shareText.contains("一起支持")) {
                    hook.showToast(context, "不支持直播!")
                    return@OnPrimaryClipChangedListener
                }
                notify.invoke(this, shareText)
            }
        }
        clipboardManager.addPrimaryClipChangedListener(primaryClipChangedListener)
    }

    // 移除剪贴板监听
    fun removeClipboardListener(context: Context) {
        primaryClipChangedListener ?: return

        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.removePrimaryClipChangedListener(primaryClipChangedListener)
        primaryClipChangedListener = null
    }
}