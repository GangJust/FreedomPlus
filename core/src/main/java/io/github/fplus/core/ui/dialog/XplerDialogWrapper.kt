package io.github.fplus.core.ui.dialog

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.fplus.plugin.dialog.XplerDialog

class XplerDialogWrapper(context: Context) : XplerDialog(context) {
    private var isShowing = mutableStateOf(false)
    private var isCancelable = mutableStateOf(true)
    private var isCanceledOnTouchOutside = mutableStateOf(true)

    private val wrapperContent = mutableStateOf<(@Composable () -> Unit)?>(null)

    init {
        window?.apply {
            setDimAmount(0f) // 取消暗色背景 (避免包装背景重叠)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 直接包装它(相当于显示了两个Dialog), 输入法不弹起文本框不能输入, 见 `TestDialog`
            if (isShowing.value) {
                Dialog(
                    onDismissRequest = {
                        dismiss()
                    },
                    properties = DialogProperties(
                        dismissOnClickOutside = isCanceledOnTouchOutside.value,
                        dismissOnBackPress = isCancelable.value,
                    )
                ) {
                    wrapperContent.value?.invoke()
                }
            }
        }
    }

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        isCanceledOnTouchOutside.value = cancel
        super.setCanceledOnTouchOutside(cancel)
    }

    override fun setCancelable(flag: Boolean) {
        isCancelable.value = flag
        super.setCancelable(flag)
    }

    override fun dismiss() {
        isShowing.value = false
        super.dismiss()
    }

    override fun show() {
        isShowing.value = true
        super.show()
    }

    fun setWrapperContent(content: @Composable () -> Unit) {
        this.wrapperContent.value = content
    }
}