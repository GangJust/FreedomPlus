package io.github.fplus.core.hook

import android.app.Dialog
import android.widget.TextView
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.text.ellipsis
import com.freegang.ktutils.view.forEachWhereChild
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HDialog : BaseHook<Dialog>() {
    companion object {
        const val TAG = "HDialog"
    }

    private val config get() = ConfigV1.get()

    private val dialogFilterKeywords by lazy {
        config.dialogFilterKeywords
            .removePrefix(",").removePrefix("，")
            .removeSuffix(",").removeSuffix("，")
            .replace("\\s".toRegex(), "")
            .replace("[,，]".toRegex(), "|")
            .toRegex()
    }

    @OnAfter("show")
    fun showAfter(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            if (!config.isDialogFilter) {
                return
            }

            val dialog = thisObject as Dialog
            val mDecorView = dialog.window?.decorView ?: return

            if (dialogFilterKeywords.pattern.isEmpty()) {
                return
            }

            mDecorView.forEachWhereChild {
                if ("${it.contentDescription}".contains(dialogFilterKeywords)) {
                    dialog.dismiss()
                    if (config.dialogDismissTips) {
                        KToastUtils.show(dialog.context, "“${it.contentDescription.ellipsis(5)}”关闭成功!")
                    }
                    return@forEachWhereChild true
                } else if (it is TextView) {
                    if ("${it.text}".contains(dialogFilterKeywords)) {
                        dialog.dismiss()
                        if (config.dialogDismissTips) {
                            KToastUtils.show(dialog.context, "“${it.text.ellipsis(5)}”关闭成功!")
                        }
                        return@forEachWhereChild true
                    }
                }

                false
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}