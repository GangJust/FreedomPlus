package com.freegang.base

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.freegang.plugin.XplerActivity
import com.freegang.ui.ModuleTheme

open class BaseActivity : XplerActivity() {
    private var isDark = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initExtraData()
    }

    private fun initExtraData() {
        isDark = intent.getBooleanExtra("isDark", false)
    }

    @Composable
    protected fun AutoTheme(
        content: @Composable () -> Unit,
    ) {
        ModuleTheme(
            isDark = isDark,
            followSystem = false,
        ) {
            content()
        }
    }
}