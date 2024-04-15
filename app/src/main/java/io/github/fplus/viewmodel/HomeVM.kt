package io.github.fplus.viewmodel

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freegang.extension.appVersionName
import com.freegang.extension.child
import com.freegang.extension.storageRootFile
import com.freegang.ktutils.app.KAppUtils
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.config.Version
import io.github.fplus.core.config.VersionConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeVM(application: Application) : AndroidViewModel(application) {
    private val app: Application get() = getApplication()

    private var _versionConfig = MutableLiveData<VersionConfig>()
    val versionConfig: LiveData<VersionConfig> = _versionConfig

    // module config
    private val config: ConfigV1 get() = ConfigV1.get()

    // FreedomPlus -> 外置存储器/Download/Freedom/
    val freedomPlusData
        get() = getApplication<Application>().storageRootFile
            .child(Environment.DIRECTORY_DCIM)
            .child("Freedom")

    // FreedomPlusNew -> 外置存储器/Download/Freedom/
    val freedomPlusNewData
        get() = getApplication<Application>().storageRootFile
            .child(Environment.DIRECTORY_DOWNLOADS)
            .child("Freedom")

    // 是否开启去插件化
    val isDisablePlugin get() = config.isDisablePlugin

    // 检查版本更新
    fun checkVersion() {
        if (KAppUtils.isAppInDebug(app)) return // 测试包不检查更新
        if (app.appVersionName.contains(Regex("beta|alpha"))) return // 非release包不检查更新
        viewModelScope.launch {
            val version = withContext(Dispatchers.IO) { Version.getRemoteReleasesLatest() }
            if (version != null) _versionConfig.value = version
        }
    }

    // 获取远程版本适配列表
    fun updateVersions() {
        if (KAppUtils.isAppInDebug(app)) return // 测试包不检查更新
        if (app.appVersionName.contains(Regex("beta|alpha"))) return // 非release包不检查更新
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val versions = Version.getVersions() ?: return@withContext
                val file = ConfigV1.getConfigDir(app).child("versions.json")
                file.writeText(versions)
            }
        }
    }

    // 版本适配提示
    suspend fun isSupportVersions(versionName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val versions = ConfigV1.getConfigDir(app).child("versions.json")
                val text = versions.readText()
                if (text.contains(versionName)) "版本功能正常" else "自行测试功能"
            } catch (e: Exception) {
                "自行测试功能"
            }
        }
    }
}