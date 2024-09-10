package io.github.fplus.core.hook

import android.app.Dialog
import android.widget.TextView
import com.freegang.extension.ellipsis
import com.freegang.extension.forEachWhereChild
import com.freegang.ktutils.app.KToastUtils
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.XplerLog
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.proxy.MethodParam

class HDialog : BaseHook() {
    companion object {
        const val TAG = "HDialog"
    }

    private val config get() = ConfigV1.get()

    private val dialogFilterKeywords by lazy {
        config.dialogFilterKeywords
            .replace("，", ",")
            .replace("\\s".toRegex(), "")
            .removePrefix(",").removeSuffix(",")
            .replace(",", "|")
            .replace("\\|+".toRegex(), "|")
            .toRegex()
    }

    override fun setTargetClass(): Class<*> {
        return Dialog::class.java
    }

    @OnAfter("show")
    fun showAfter(params: MethodParam) {
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