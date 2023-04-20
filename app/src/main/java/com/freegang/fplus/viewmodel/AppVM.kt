package com.freegang.fplus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

open class AppVM(application: Application) : AndroidViewModel(application) {
    private var _isDark = MutableLiveData(false)
    val isDark: LiveData<Boolean> = _isDark

    // 切换主题模式(亮色/暗色)
    fun toggleThemeModel() {
        _isDark.value = !_isDark.value!!
    }
}