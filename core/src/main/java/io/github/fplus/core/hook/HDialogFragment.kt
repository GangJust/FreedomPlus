package io.github.fplus.core.hook

import android.content.Context
import androidx.fragment.app.DialogFragment
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.methodInvokeFirst
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.argsOrEmpty
import io.github.xpler.core.hookBlockRunning

class HDialogFragment(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<DialogFragment>(lpparam) {
    companion object {
        const val TAG = "HDialogFragment"
    }

    @OnBefore("show")
    fun showBefore(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            KLogCat.d(
                "弹窗: $thisObject",
                "参数: ${argsOrEmpty.joinToString()}",
            )

            val context = thisObject.methodInvokeFirst("getContext") as Context
            KToastUtils.showOriginal(context, "弹窗: $thisObject")
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}