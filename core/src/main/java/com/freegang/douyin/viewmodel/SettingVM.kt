package com.freegang.douyin.viewmodel

import android.app.Application
import android.content.res.AssetManager
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freegang.config.Config
import com.freegang.config.Version
import com.freegang.config.VersionConfig
import com.freegang.ktutils.app.readAssetsAsText
import com.freegang.ktutils.io.child
import com.freegang.ktutils.io.storageRootFile
import com.freegang.webdav.WebDav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class SettingVM(application: Application) : AndroidViewModel(application) {

    private var _versionConfig = MutableLiveData<VersionConfig>()
    val versionConfig: LiveData<VersionConfig> = _versionConfig

    // module config
    private lateinit var config: Config

    private var _isOwnerDir = MutableLiveData(false)
    val isOwnerDir: LiveData<Boolean> = _isOwnerDir

    private var _isDownload = MutableLiveData(false)
    val isDownload: LiveData<Boolean> = _isDownload

    private var _isEmoji = MutableLiveData(false)
    val isEmoji: LiveData<Boolean> = _isEmoji

    private var _isVibrate = MutableLiveData(false)
    val isVibrate: LiveData<Boolean> = _isVibrate

    private var _isTranslucent = MutableLiveData(false)
    val isTranslucent: LiveData<Boolean> = _isTranslucent

    private var _isNeat = MutableLiveData(false)
    val isNeat: LiveData<Boolean> = _isNeat

    private var _isLongPressMode = MutableLiveData(false)
    val isLongPressMode: LiveData<Boolean> = _isLongPressMode

    private var _isNotification = MutableLiveData(false)
    val isNotification: LiveData<Boolean> = _isNotification

    private var _isWebDav = MutableLiveData(false)
    var isWebDav: LiveData<Boolean> = _isWebDav

    private var _webDavHost = MutableLiveData("")
    var webDavHost: LiveData<String> = _webDavHost

    private var _webDavUsername = MutableLiveData("")
    var webDavUsername: LiveData<String> = _webDavUsername

    private var _webDavPassword = MutableLiveData("")
    var webDavPassword: LiveData<String> = _webDavPassword

    private var _isHideTab = MutableLiveData(false)
    var isHideTab: LiveData<Boolean> = _isHideTab

    private var _hideTabKeywords = MutableLiveData("")
    var hideTabKeywords: LiveData<String> = _hideTabKeywords


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

    // 读取模块配置
    fun loadConfig() {
        viewModelScope.launch {
            config = withContext(Dispatchers.IO) { Config.read(getApplication()) }
            changeIsOwnerDir(config.isOwnerDir)
            changeIsDownload(config.isDownload)
            changeIsEmoji(config.isEmoji)
            changeIsVibrate(config.isVibrate)
            changeIsTranslucent(config.isTranslucent)
            changeIsNeat(config.isNeat)
            changeIsLongPressMode(config.isLongPressMode)
            changeIsNotification(config.isNotification)
            changeIsWebDav(config.isWebDav)
            setWebDavConfig(config.webDavHost, config.webDavUsername, config.webDavPassword)
            changeIsHideTab(config.isHideTab)
            setHideTabKeywords(config.hideTabKeywords)
        }
    }

    // 视频创作者单独创建文件夹
    fun changeIsOwnerDir(value: Boolean) {
        _isOwnerDir.value = value
        config.isOwnerDir = value
    }

    // 视频/图文/音乐下载
    fun changeIsDownload(value: Boolean) {
        _isDownload.value = value
        config.isDownload = value
    }

    // 表情包保存
    fun changeIsEmoji(value: Boolean) {
        _isEmoji.value = value
        config.isEmoji = value
    }

    // 震动反馈保存
    fun changeIsVibrate(value: Boolean) {
        _isVibrate.value = value
        config.isVibrate = value
    }

    // 首页控件半透明
    fun changeIsTranslucent(value: Boolean) {
        _isTranslucent.value = value
        config.isTranslucent = value
    }

    // 清爽模式
    fun changeIsNeat(value: Boolean) {
        _isNeat.value = value
        config.isNeat = value
    }


    //清爽模式弹窗响应模式
    fun changeIsLongPressMode(value: Boolean) {
        _isLongPressMode.value = value
        config.isLongPressMode = value
    }

    // 是否通知栏下载
    fun changeIsNotification(value: Boolean) {
        _isNotification.value = value
        config.isNotification = value
    }

    // WebDav
    fun changeIsWebDav(value: Boolean) {
        _isWebDav.value = value
        config.isWebDav = value
    }

    // 初始化WebDav
    fun initWebDav(block: (Boolean) -> Unit) {
        if (!hasWebDavConfig()) return

        viewModelScope.launch {
            try {
                val webDav = WebDav(webDavHost.value!!, webDavUsername.value!!, webDavPassword.value!!)
                if (!webDav.exists("Freedom", isDirectory = true)) {
                    webDav.createDirectory("Freedom")
                }
                if (!webDav.exists("数据目录，谨慎删除.txt", "Freedom")) {
                    webDav.put("数据目录，谨慎删除.txt", "Freedom", bytes = "该目录为Freedom数据目录，请谨慎删除!!!".toByteArray())
                }
                block.invoke(true)
            } catch (e: IOException) {
                e.printStackTrace()
                block.invoke(false)
            }
        }
    }

    // WebDav配置是否存在
    fun hasWebDavConfig(): Boolean {
        val host = webDavHost.value ?: return false
        val username = webDavUsername.value ?: return false
        val password = webDavPassword.value ?: return false

        return !(host.isBlank() or username.isBlank() or password.isBlank())
    }

    // 保存WebDav配置
    fun setWebDavConfig(host: String, username: String, password: String) {
        _webDavHost.value = host
        _webDavUsername.value = username
        _webDavPassword.value = password

        config.webDavHost = host
        config.webDavUsername = username
        config.webDavPassword = password
    }

    // 隐藏顶部tab
    fun changeIsHideTab(value: Boolean) {
        _isHideTab.value = value
        config.isHideTab = value
    }

    // 保存顶部tab包含的关键字, 逗号隔开
    fun setHideTabKeywords(hideTabKeywords: String) {
        _hideTabKeywords.value = hideTabKeywords
        config.hideTabKeywords = hideTabKeywords
    }

    // 保存模块配置
    fun saveModuleConfig(asset: AssetManager) {
        config.isOwnerDir = isOwnerDir.value ?: false
        config.isDownload = isDownload.value ?: false
        config.isEmoji = isEmoji.value ?: false
        config.isVibrate = isVibrate.value ?: false
        config.isTranslucent = isTranslucent.value ?: false
        config.isNeat = isNeat.value ?: false
        config.isLongPressMode = isLongPressMode.value ?: false
        config.isNotification = isNotification.value ?: false
        config.isWebDav = isWebDav.value ?: false
        config.webDavHost = webDavHost.value ?: ""
        config.webDavUsername = webDavUsername.value ?: ""
        config.webDavPassword = webDavPassword.value ?: ""
        config.isHideTab = isHideTab.value ?: false
        config.hideTabKeywords = hideTabKeywords.value ?: ""


        val version = asset.readAssetsAsText("version").split("-")
        config.isSupportHint = version[1].toLong() != config.versionCode
        config.versionName = version[0]
        config.versionCode = version[1].toLong()

        config.save(getApplication())
    }

    // 图片迁移
    fun migratePicture() {
        val userDirs = freedomPlusData.child("picture").listFiles() ?: return
        for (userDir in userDirs) {
            if (userDir.isFile) continue
            val pictureDirs = userDir.listFiles() ?: continue
            //println(userDir.absolutePath)

            for (pictureDir in pictureDirs) {
                if (pictureDir.isFile) continue
                if (pictureDir.list()?.size == 0) {
                    pictureDir.delete()
                    continue
                }
                val pictures = pictureDir.listFiles() ?: continue
                //println(pictureDir.absolutePath)

                for (picture in pictures) {
                    //println(picture.absolutePath)
                    picture.copyTo(File(userDir, "${pictureDir.name}_${picture.name}"), true)
                }

                pictureDir.deleteRecursively()
            }
        }
    }
}