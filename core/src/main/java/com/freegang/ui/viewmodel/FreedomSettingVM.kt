package com.freegang.ui.viewmodel

import android.app.Application
import android.content.res.AssetManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freegang.config.ConfigV1
import com.freegang.config.Version
import com.freegang.config.VersionConfig
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.appVersionCode
import com.freegang.ktutils.app.appVersionName
import com.freegang.ktutils.app.readAssetsAsText
import com.freegang.ktutils.json.isEmpty
import com.freegang.ktutils.net.KUrlUtils
import com.freegang.webdav.WebDav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

class FreedomSettingVM(application: Application) : AndroidViewModel(application) {
    val app: Application get() = getApplication()

    private var _versionConfig = MutableLiveData<VersionConfig>()
    val versionConfig: LiveData<VersionConfig> = _versionConfig

    // module config
    private lateinit var config: ConfigV1

    private var _isDownload = MutableLiveData(false)
    val isDownload: LiveData<Boolean> = _isDownload

    private var _isOwnerDir = MutableLiveData(false)
    val isOwnerDir: LiveData<Boolean> = _isOwnerDir

    private var _isNotification = MutableLiveData(false)
    val isNotification: LiveData<Boolean> = _isNotification

    private var _isEmoji = MutableLiveData(false)
    val isEmoji: LiveData<Boolean> = _isEmoji

    private var _isVibrate = MutableLiveData(false)
    val isVibrate: LiveData<Boolean> = _isVibrate

    private var _isTranslucent = MutableLiveData(false)
    val isTranslucent: LiveData<Boolean> = _isTranslucent

    private var _isDisableDoubleLike = MutableLiveData(false)
    val isDisableDoubleLike: LiveData<Boolean> = _isDisableDoubleLike

    private var _isHidePhotoButton= MutableLiveData(false)
    val isDHidePhotoButton: LiveData<Boolean> = _isHidePhotoButton

    private var _isDisablePhotoButton= MutableLiveData(false)
    val isDisablePhotoButton: LiveData<Boolean> = _isDisablePhotoButton

    private var _isVideoFilter = MutableLiveData(false)
    val isVideoFilter: LiveData<Boolean> = _isVideoFilter

    private var _videoFilterKeywords = MutableLiveData("")
    var videoFilterKeywords: LiveData<String> = _videoFilterKeywords

    private var _isNeatMode = MutableLiveData(false)
    val isNeatMode: LiveData<Boolean> = _isNeatMode

    private var _isLongPressMode = MutableLiveData(false)
    val isLongPressMode: LiveData<Boolean> = _isLongPressMode

    private var _isHideTab = MutableLiveData(false)
    var isHideTab: LiveData<Boolean> = _isHideTab

    private var _hideTabKeywords = MutableLiveData("")
    var hideTabKeywords: LiveData<String> = _hideTabKeywords

    private var _isWebDav = MutableLiveData(false)
    var isWebDav: LiveData<Boolean> = _isWebDav

    private var _webDavHost = MutableLiveData("")
    var webDavHost: LiveData<String> = _webDavHost

    private var _webDavUsername = MutableLiveData("")
    var webDavUsername: LiveData<String> = _webDavUsername

    private var _webDavPassword = MutableLiveData("")
    var webDavPassword: LiveData<String> = _webDavPassword

    private var _webDavHistory = MutableLiveData(emptyList<WebDav.Config>())
    var webDavHistory: LiveData<List<WebDav.Config>> = _webDavHistory

    private var _isTimedExit = MutableLiveData(false)
    var isTimedExit: LiveData<Boolean> = _isTimedExit

    private var _timedExitValue = MutableLiveData("")
    var timedExitValue: LiveData<String> = _timedExitValue

    private var _isDisablePlugin = MutableLiveData(false)
    val isDisablePlugin: LiveData<Boolean> = _isDisablePlugin


    // 检查版本更新
    fun checkVersion() {
        if (KAppUtils.isAppInDebug(app)) return // 测试包不检查更新
        if (app.appVersionName.contains(Regex("beta|alpha"))) return // 非release包不检查更新
        viewModelScope.launch {
            val version = withContext(Dispatchers.IO) { Version.getRemoteReleasesLatest() }
            if (version != null) _versionConfig.value = version
        }
    }

    // 读取模块配置
    fun loadConfig() {
        viewModelScope.launch {
            config = withContext(Dispatchers.IO) { ConfigV1.get() }
            changeIsDownload(config.isDownload)
            changeIsOwnerDir(config.isOwnerDir)
            changeIsNotification(config.isNotification)
            changeIsEmoji(config.isEmoji)
            changeIsVibrate(config.isVibrate)
            changeIsTranslucent(config.isTranslucent)
            changeIsNeatMode(config.isNeatMode)
            changeIsDisableDoubleLike(config.isDisableDoubleLike)
            changeIsHidePhotoButton(config.isHidePhotoButton)
            changeIsDisablePhotoButton(config.isDisablePhotoButton)
            changeIsVideoFilter(config.isVideoFilter)
            setVideoFilterKeywords(config.videoFilterKeywords)
            changeLongPressMode(config.longPressMode)
            changeIsHideTab(config.isHideTab)
            setHideTabKeywords(config.hideTabKeywords)
            changeIsWebDav(config.isWebDav)
            loadWebHistory()
            setWebDavConfig(config.webDavConfig)
            changeIsTimeExit(config.isTimedExit)
            setTimedExitValue(config.timedExitValue)
            changeIsDisablePlugin(config.isDisablePlugin)
        }
    }

    // 视频/图文/音乐下载
    fun changeIsDownload(value: Boolean) {
        _isDownload.value = value
        config.isDownload = value
    }

    // 视频创作者单独创建文件夹
    fun changeIsOwnerDir(value: Boolean) {
        _isOwnerDir.value = value
        config.isOwnerDir = value
    }

    // 通知栏下载
    fun changeIsNotification(value: Boolean) {
        _isNotification.value = value
        config.isNotification = value
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

    // 禁用双击点赞
    fun changeIsDisableDoubleLike(value: Boolean) {
        _isDisableDoubleLike.value = value
        config.isDisableDoubleLike = value
    }

    // 隐藏底部加号按钮
    fun changeIsHidePhotoButton(value: Boolean) {
        _isHidePhotoButton.value = value
        config.isHidePhotoButton = value
    }

    // 禁止拍摄
    fun changeIsDisablePhotoButton(value: Boolean) {
        _isDisablePhotoButton.value = value
        config.isDisablePhotoButton = value
    }

    val videoFilterTypes get() = config.videoFilterTypes

    // 视频过滤
    fun changeIsVideoFilter(value: Boolean) {
        _isVideoFilter.value = value
        config.isVideoFilter = value
    }

    // 视频过滤关键字
    fun setVideoFilterKeywords(value: String) {
        _videoFilterKeywords.value = value
        config.videoFilterKeywords = value
    }

    // 清爽模式
    fun changeIsNeatMode(value: Boolean) {
        _isNeatMode.value = value
        config.isNeatMode = value
    }

    // 清爽模式弹窗响应模式
    fun changeLongPressMode(value: Boolean) {
        _isLongPressMode.value = value
        config.longPressMode = value
    }

    // 隐藏顶部tab
    fun changeIsHideTab(value: Boolean) {
        _isHideTab.value = value
        config.isHideTab = value
    }

    // 隐藏顶部tab包含的关键字, 逗号隔开
    fun setHideTabKeywords(hideTabKeywords: String) {
        _hideTabKeywords.value = hideTabKeywords
        config.hideTabKeywords = hideTabKeywords
    }

    // WebDav
    fun changeIsWebDav(value: Boolean) {
        _isWebDav.value = value
        config.isWebDav = value
    }

    // 初始化WebDav
    fun initWebDav(block: (Boolean, String) -> Unit) {
        if (!hasWebDavConfig()) {
            block.invoke(false, "请填写WebDav配置!")
            return
        }
        if (!KUrlUtils.isValidUrl(webDavHost.value!!)) {
            block.invoke(false, "WebDav地址格式有误!")
            return
        }
        viewModelScope.launch {
            try {
                val webDav = WebDav(webDavHost.value!!, webDavUsername.value!!, webDavPassword.value!!)
                if (!webDav.exists("Freedom", isDirectory = true)) {
                    webDav.createDirectory("Freedom")
                }
                if (!webDav.exists("数据目录，谨慎删除.txt", "Freedom")) {
                    webDav.put("数据目录，谨慎删除.txt", "Freedom", bytes = "该目录为Freedom数据目录，请谨慎删除!!!".toByteArray())
                }
                block.invoke(true, "WebDav连接成功!")
            } catch (e: IOException) {
                e.printStackTrace()
                block.invoke(false, "WebDav连接失败!")
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

    // 读取WebDav历史
    private fun loadWebHistory() {
        _webDavHistory.value = config.webDavConfigList
    }

    // 保存WebDav配置
    fun setWebDavConfig(webDavConfig: WebDav.Config) {
        _webDavHost.value = webDavConfig.host
        _webDavUsername.value = webDavConfig.username
        _webDavPassword.value = webDavConfig.password
        config.webDavConfig = webDavConfig
    }

    // 增加WebDav历史
    fun addWebDavConfig(webDavConfig: WebDav.Config) {
        config.addWebDavConfig(webDavConfig)
        loadWebHistory()
    }

    // 移除WebDav历史
    fun removeWebDavConfig(webDavConfig: WebDav.Config) {
        config.removeWebDavConfig(webDavConfig)
        loadWebHistory()
    }

    // 定时退出
    fun changeIsTimeExit(value: Boolean) {
        _isTimedExit.value = value
        config.isTimedExit = value
    }

    // 运行分钟数, 空闲分钟数
    fun setTimedExitValue(value: String) {
        _timedExitValue.value = value
        config.timedExitValue = value
    }

    // 去插件化
    fun changeIsDisablePlugin(value: Boolean) {
        _isDisablePlugin.value = value
        config.isDisablePlugin = value
    }

    // 保存版本信息
    fun setVersionConfig(asset: AssetManager) {
        val version = asset.readAssetsAsText("version")
        val versionName = version.substringBeforeLast("-")
        val versionCode = version.substringAfterLast("-")
        config.versionConfig = ConfigV1.Version(
            versionName,
            versionCode.toLong(),
            app.appVersionName,
            app.appVersionCode
        )
    }

    val hasClasses get() = !config.classes.isEmpty

    // 清除类日志
    fun clearClasses() {
        config.classes = JSONObject()
    }
}