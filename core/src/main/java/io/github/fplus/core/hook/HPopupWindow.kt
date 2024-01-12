package io.github.fplus.core.hook

import android.content.Context
import android.widget.PopupWindow
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGetFirst
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.xpler.core.argsOrEmpty
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning

class HPopupWindow : BaseHook<PopupWindow>() {
    companion object {
        const val TAG = "HPopupWindow"
    }

    @OnBefore("showAtLocation")
    fun showAtLocationBefore(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            KLogCat.d(
                "弹窗: $thisObject",
                "参数: ${argsOrEmpty.joinToString()}"
            )

            val context = thisObject.fieldGetFirst("mContext") as Context
            KToastUtils.showOriginal(context, "弹窗: $thisObject")
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}