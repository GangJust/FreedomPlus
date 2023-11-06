package com.freegang.ui.dialog

import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue

open class TestDialog(context: Context) : XplerDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LazyColumn {
                item {
                    // 正常弹窗, 但是不能输入 (Jetpack的Bug) -- 破案了，主题的问题
                    var value by remember { mutableStateOf(TextFieldValue()) }
                    TextField(
                        value = value,
                        onValueChange = {
                            value = it
                        },
                    )

                    // 正常弹窗, 能正常输入
                    /*AndroidView(factory = {
                        EditText(it)
                    })*/
                }
            }
        }
    }

}