package com.freegang.fplus.viewmodel

import android.app.Application
import android.content.res.AssetManager
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freegang.config.ConfigV1
import com.freegang.config.Version
import com.freegang.config.VersionConfig
import com.freegang.ktutils.app.appVersionCode
import com.freegang.ktutils.app.appVersionName
import com.freegang.ktutils.app.readAssetsAsText
import com.freegang.ktutils.io.child
import com.freegang.ktutils.io.storageRootFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeVM(application: Application) : AndroidViewModel(application) {
    val app: Application get() = getApplication()

    private var _versionConfig = MutableLiveData<VersionConfig>()
    val versionConfig: LiveData<VersionConfig> = _versionConfig

    // module config
    private val config: ConfigV1 get() = ConfigV1.get()

    // 检查版本更新
    fun checkVersion() {
        viewModelScope.launch {
            val version = withContext(Dispatchers.IO) { Version.getRemoteReleasesLatest() }
            if (version != null) _versionConfig.value = version
        }
    }

    // Freedom -> 外置存储器/Download/Freedom/
    val freedomData
        get() = getApplication<Application>().storageRootFile
            .child(Environment.DIRECTORY_DOWNLOADS)
            .child("Freedom")

    // FreedomPlus -> 外置存储器/DCIM/Freedom/
    val freedomPlusData
        get() = getApplication<Application>().storageRootFile
            .child(Environment.DIRECTORY_DCIM)
            .child("Freedom")

    // 是否显示兼容提示
    fun isSupportHint(value: Boolean) {
        config.isSupportHint = value
    }

    // 保存版本信息
    fun setVersionConfig(asset: AssetManager) {
        val version = asset.readAssetsAsText("version").split("-")
        config.isSupportHint = version[1].toLong() != config.versionConfig.versionCode
        config.versionConfig = ConfigV1.Version(
            version[0],
            version[1].toLong(),
            app.appVersionName,
            app.appVersionCode
        )
    }

    fun isSupportVersions(versionName: String): String {
        val list = listOf(
            "23.5.0", "23.6.0", "23.7.0", "23.8.0", "23.9.0",
            "24.0.0", "24.1.0", "24.2.0", "24.3.0", "24.4.0",
            "24.5.0", "24.6.0", "24.7.0", "24.8.0", "24.9.0",
            "25.0.0", "25.1.0", "25.2.0", "25.3.0", "25.4.0",
            "25.5.0", "25.6.0", "25.7.0", "25.8.0", "25.9.0",
            "26.0.0", "26.1.0", "26.2.0"
        )
        return if (list.contains(versionName)) "版本功能正常" else "自行测试功能"
    }
}