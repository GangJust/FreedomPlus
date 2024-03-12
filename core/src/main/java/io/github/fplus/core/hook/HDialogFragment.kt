package io.github.fplus.core.hook

import android.content.Context
import androidx.fragment.app.DialogFragment
import com.freegang.ktutils.reflect.methodInvoke
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HDialogFragment : BaseHook<DialogFragment>() {
    companion object {
        const val TAG = "HDialogFragment"
    }

    @OnBefore("show")
    fun showBefore(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            val context = thisObject.methodInvoke("getContext") as Context
        }.onFailure {
            XplerLog.e(it)
        }
    }
}