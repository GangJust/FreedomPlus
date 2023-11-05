package com.freegang.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import com.freegang.ktutils.reflect.fieldSetFirst

open class XplerDialog(context: Context) : BaseXplerDialog(context) {
    private val content = mutableStateOf<(@Composable () -> Unit)?>(null)

    init {
        val contextThemeWrapper = XplerContextThemeWrapper(context)
        this.fieldSetFirst("mContext", contextThemeWrapper)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*runCatching {
            KLogCat.d("插件资源: ${context.resources.getString(R.string.app_name)}")
        }.onFailure {
            KLogCat.d("获取异常: \n\n${it.stackTraceToString()}")
        }*/

        setContentView(ComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setContent {
                content.value?.invoke()
            }
        })
    }

    fun setContent(content: @Composable () -> Unit) {
        this.content.value = content
    }
}