package io.github.fplus.core.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.EditText
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Switch
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.viewinterop.AndroidView
import io.github.fplus.plugin.dialog.XplerDialog
import io.github.fplus.core.ui.ModuleTheme

open class TestDialog(context: Context) : XplerDialog(context) {

    init {
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setDimAmount(0f)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Test()
        }
    }

    @Composable
    private fun Test() {
        ModuleTheme {
            LazyColumn {
                item {
                    // 正常弹窗, 但是不能输入 (Jetpack的Bug) -- 破案了，主题的问题(存疑)
                    var value by remember { mutableStateOf(TextFieldValue()) }
                    TextField(
                        value = value,
                        onValueChange = {
                            value = it
                        },
                    )
                }

                item {
                    // 正常弹窗, 能正常输入
                    AndroidView(factory = {
                        EditText(it)
                    })
                }

                item {
                    var value by remember { mutableStateOf(false) }
                    Switch(
                        checked = value,
                        onCheckedChange = {
                            value = it
                        },
                    )
                }
            }
        }
    }
}