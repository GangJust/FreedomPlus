package io.github.fplus.core.ui.viewmodel

import android.app.Application
import android.content.res.AssetManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freegang.extension.appVersionCode
import com.freegang.extension.appVersionName
import com.freegang.extension.isEmpty
import com.freegang.extension.readAssetsAsText
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.net.KUrlUtils
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.config.Version
import io.github.fplus.core.config.VersionConfig
import io.github.webdav.WebDav
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

    private var _ownerDir = MutableLiveData(false)
    val isOwnerDir: LiveData<Boolean> = _ownerDir

    private var _notificationDownload = MutableLiveData(false)
    val isNotification: LiveData<Boolean> = _notificationDownload

    private var _isCopyLinkDownload = MutableLiveData(false)
    val isCopyDownload: LiveData<Boolean> = _isCopyLinkDownload

    private var _videoCoding = MutableLiveData("")
    val videoCoding: LiveData<String> = _videoCoding

    private var _isEmojiDownload = MutableLiveData(false)
    val isEmoji: LiveData<Boolean> = _isEmojiDownload

    private var _vibrate = MutableLiveData(false)
    val isVibrate: LiveData<Boolean> = _vibrate

    private var _isTranslucent = MutableLiveData(false)
    val isTranslucent: LiveData<Boolean> = _isTranslucent

    private var _translucentValue = MutableLiveData(listOf(50, 50, 50, 50))
    val translucentValue: LiveData<List<Int>> = _translucentValue

    private var _isRemoveSticker = MutableLiveData(false)
    val isRemoveSticker: LiveData<Boolean> = _isRemoveSticker

    private var _isRemoveBottomCtrlBar = MutableLiveData(false)
    val isRemoveBottomCtrlBar: LiveData<Boolean> = _isRemoveBottomCtrlBar

    private var _isPreventRecalled = MutableLiveData(false)
    val isPreventRecalled: LiveData<Boolean> = _isPreventRecalled

    private var _preventRecalledOtherSetting = MutableLiveData(listOf(false))
    val preventRecalledOtherSetting: LiveData<List<Boolean>> = _preventRecalledOtherSetting

    private var _isDoubleClickType = MutableLiveData(false)
    val isDoubleClickType: LiveData<Boolean> = _isDoubleClickType

    private var _doubleClickType = MutableLiveData(2)
    val doubleClickType: LiveData<Int> = _doubleClickType

    private var _isLongtimeVideoToast = MutableLiveData(false)
    val isLongtimeVideoToast: LiveData<Boolean> = _isLongtimeVideoToast

    private var _isHideTopTab = MutableLiveData(false)
    val isHideTopTab: LiveData<Boolean> = _isHideTopTab

    private var _hideTopTabKeywords = MutableLiveData("")
    val hideTopTabKeywords: LiveData<String> = _hideTopTabKeywords

    private var _isHideBottomTab = MutableLiveData(false)
    val isHideBottomTab: LiveData<Boolean> = _isHideBottomTab

    private var _hideBottomTabKeywords = MutableLiveData("")
    val hideBottomTabKeywords: LiveData<String> = _hideBottomTabKeywords

    private var _isHidePhotoButton = MutableLiveData(false)
    val isHidePhotoButton: LiveData<Boolean> = _isHidePhotoButton

    private var _photoButtonType = MutableLiveData(2)
    val photoButtonType: LiveData<Int> = _photoButtonType

    private var _isPreventAccidentalTouch = MutableLiveData(false)
    val isPreventAccidentalTouch: LiveData<Boolean> = _isPreventAccidentalTouch

    private var _isVideoOptionBarFilter = MutableLiveData(false)
    val isVideoOptionBarFilter: LiveData<Boolean> = _isVideoOptionBarFilter

    private var _videoOptionBarFilterKeywords = MutableLiveData("")
    val videoOptionBarFilterKeywords: LiveData<String> = _videoOptionBarFilterKeywords

    private var _isVideoFilter = MutableLiveData(false)
    val isVideoFilter: LiveData<Boolean> = _isVideoFilter

    private var _videoFilterKeywords = MutableLiveData("")
    val videoFilterKeywords: LiveData<String> = _videoFilterKeywords

    private var _isDialogFilter = MutableLiveData(false)
    val isDialogFilter: LiveData<Boolean> = _isDialogFilter

    private var _dialogDismissTips = MutableLiveData(false)
    val dialogDismissTips: LiveData<Boolean> = _dialogDismissTips

    private var _dialogFilterKeywords = MutableLiveData("")
    val dialogFilterKeywords: LiveData<String> = _dialogFilterKeywords

    private var _isNeatMode = MutableLiveData(false)
    val isNeatMode: LiveData<Boolean> = _isNeatMode

    private var _isImmersive = MutableLiveData(false)
    val isImmersive: LiveData<Boolean> = _isImmersive

    private var _systemControllerValue = MutableLiveData(listOf(false, false))
    val systemControllerValue: LiveData<List<Boolean>> = _systemControllerValue

    private var _longPressMode = MutableLiveData(false)
    val longPressMode: LiveData<Boolean> = _longPressMode

    private var _isCommentColorMode = MutableLiveData(false)
    val isCommentColorMode: LiveData<Boolean> = _isCommentColorMode

    private var _commentColorMode = MutableLiveData(0)
    val commentColorMode: LiveData<Int> = _commentColorMode

    private var _isWebDav = MutableLiveData(false)
    val isWebDav: LiveData<Boolean> = _isWebDav

    private var _webDavHost = MutableLiveData("")
    val webDavHost: LiveData<String> = _webDavHost

    private var _webDavUsername = MutableLiveData("")
    val webDavUsername: LiveData<String> = _webDavUsername

    private var _webDavPassword = MutableLiveData("")
    val webDavPassword: LiveData<String> = _webDavPassword

    private var _webDavHistory = MutableLiveData(emptyList<WebDav.Config>())
    val webDavHistory: LiveData<List<WebDav.Config>> = _webDavHistory

    private var _isTimedExit = MutableLiveData(false)
    val isTimedExit: LiveData<Boolean> = _isTimedExit

    private var _timedShutdownValue = MutableLiveData(listOf(10, 3))
    val timedShutdownValue: LiveData<List<Int>> = _timedShutdownValue

    private var _keepAppBackend = MutableLiveData(false)
    val keepAppBackend: LiveData<Boolean> = _keepAppBackend

    private var _isCrashTolerance = MutableLiveData(false)
    val isCrashTolerance: LiveData<Boolean> = _isCrashTolerance

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
            setOwnerDir(config.ownerDir)
            setNotificationDownload(config.notificationDownload)
            setCopyLinkDownload(config.copyLinkDownload)
            setVideoCoding(config.videoCoding)
            changeIsEmojiDownload(config.isEmojiDownload)
            setVibrate(config.vibrate)
            changeIsTranslucent(config.isTranslucent)
            setTranslucentValue(config.translucentValue)
            changeIsRemoveSticker(config.isRemoveSticker)
            changeIsRemoveBottomCtrlBar(config.isRemoveBottomCtrlBar)
            changeIsPreventRecalled(config.isPreventRecalled)
            changePreventRecalledOtherSetting(config.preventRecalledOtherSetting)
            changeIsDoubleClickType(config.isDoubleClickType)
            setDoubleClickType(config.doubleClickType)
            changeIsLongtimeVideoToast(config.isLongtimeVideoToast)
            changeIsVideoOptionBarFilter(config.isVideoOptionBarFilter)
            setVideoOptionBarFilterKeywords(config.videoOptionBarFilterKeywords)
            changeIsVideoFilter(config.isVideoFilter)
            setVideoFilterKeywords(config.videoFilterKeywords)
            changeIsDialogFilter(config.isDialogFilter)
            setDialogDismissTips(config.dialogDismissTips)
            setDialogFilterKeywords(config.dialogFilterKeywords)
            changeIsNeatMode(config.isNeatMode)
            setLongPressMode(config.longPressMode)
            changeIsImmersive(config.isImmersive)
            setSystemControllerValue(config.systemControllerValue)
            changeIsCommentColorMode(config.isCommentColorMode)
            changeCommentColorMode(config.commentColorMode)
            changeIsHideTopTab(config.isHideTopTab)
            setHideTopTabKeywords(config.hideTopTabKeywords)
            changeIsHideBottomTab(config.isHideBottomTab)
            setHideBottomTabKeywords(config.hideBottomTabKeywords)
            changeIsHidePhotoButton(config.isHidePhotoButton)
            setPhotoButtonType(config.photoButtonType)
            changeIsPreventAccidentalTouch(config.isPreventAccidentalTouch)
            changeIsWebDav(config.isWebDav)
            loadWebHistory()
            setWebDavConfig(config.webDavConfig)
            changeIsTimeExit(config.isTimedExit)
            setTimedShutdownValue(config.timedShutdownValue)
            setKeepAppBackend(config.keepAppBackend)
            changeIsCrashTolerance(config.isCrashTolerance)
            changeIsDisablePlugin(config.isDisablePlugin)
        }
    }

    // 视频/图文/音乐下载
    fun changeIsDownload(value: Boolean) {
        _isDownload.value = value
        config.isDownload = value
    }

    // 视频创作者单独创建文件夹
    fun setOwnerDir(value: Boolean) {
        _ownerDir.value = value
        config.ownerDir = value
    }

    // 通知栏下载
    fun setNotificationDownload(value: Boolean) {
        _notificationDownload.value = value
        config.notificationDownload = value
    }

    // 复制链接时弹出下载
    fun setCopyLinkDownload(value: Boolean) {
        _isCopyLinkDownload.value = value
        config.copyLinkDownload = value
    }

    // 视频编码类型
    fun setVideoCoding(value: String) {
        _videoCoding.value = value
        config.videoCoding = value
    }

    // 表情包保存
    fun changeIsEmojiDownload(value: Boolean) {
        _isEmojiDownload.value = value
        config.isEmojiDownload = value
    }

    // 震动反馈
    fun setVibrate(value: Boolean) {
        _vibrate.value = value
        config.vibrate = value
    }

    // 首页控件半透明
    fun changeIsTranslucent(value: Boolean) {
        _isTranslucent.value = value
        config.isTranslucent = value
    }

    /// 首页控件透明度
    fun setTranslucentValue(value: List<Int>) {
        _translucentValue.value = value
        config.translucentValue = value
    }

    // 移除悬浮挑战/评论贴纸
    fun changeIsRemoveSticker(value: Boolean) {
        _isRemoveSticker.value = value
        config.isRemoveSticker = value
    }

    // 移除底部播放控制栏
    fun changeIsRemoveBottomCtrlBar(value: Boolean) {
        _isRemoveBottomCtrlBar.value = value
        config.isRemoveBottomCtrlBar = value
    }

    // 消息防撤回
    fun changeIsPreventRecalled(value: Boolean) {
        _isPreventRecalled.value = value
        config.isPreventRecalled = value
    }

    // 防撤回其他设置
    fun changePreventRecalledOtherSetting(value: List<Boolean>) {
        _preventRecalledOtherSetting.value = value
        config.preventRecalledOtherSetting = value
    }

    // 是否开启更改双击响应类型
    fun changeIsDoubleClickType(value: Boolean) {
        _isDoubleClickType.value = value
        config.isDoubleClickType = value
    }

    // 双击响应类型
    fun setDoubleClickType(value: Int) {
        _doubleClickType.value = value
        config.doubleClickType = value
    }

    // 视频时长超过10分钟提示
    fun changeIsLongtimeVideoToast(value: Boolean) {
        _isLongtimeVideoToast.value = value
        config.isLongtimeVideoToast = value
    }

    // 隐藏顶部tab
    fun changeIsHideTopTab(value: Boolean) {
        _isHideTopTab.value = value
        config.isHideTopTab = value
    }

    // 隐藏顶部tab包含的关键字, 逗号隔开
    fun setHideTopTabKeywords(keywords: String) {
        _hideTopTabKeywords.value = keywords
        config.hideTopTabKeywords = keywords
    }

    // 隐藏底部tab
    fun changeIsHideBottomTab(value: Boolean) {
        _isHideBottomTab.value = value
        config.isHideBottomTab = value
    }

    // 隐藏底部tab包含的关键字, 逗号隔开
    fun setHideBottomTabKeywords(keywords: String) {
        _hideBottomTabKeywords.value = keywords
        config.hideBottomTabKeywords = keywords
    }

    // 隐藏底部加号按钮
    fun changeIsHidePhotoButton(value: Boolean) {
        _isHidePhotoButton.value = value
        config.isHidePhotoButton = value
    }

    // 改变底部加号拍摄模式
    fun setPhotoButtonType(value: Int) {
        _photoButtonType.value = value
        config.photoButtonType = value
    }

    // 手势误触复确认
    fun changeIsPreventAccidentalTouch(value: Boolean) {
        _isPreventAccidentalTouch.value = value
        config.isPreventAccidentalTouch = value
    }

    // 视频右侧控件栏
    fun changeIsVideoOptionBarFilter(value: Boolean) {
        _isVideoOptionBarFilter.value = value
        config.isVideoOptionBarFilter = value
    }

    val videoOptionBarFilterTypes get() = config.videoOptionBarFilterTypes

    // 视频过滤关键字
    fun setVideoOptionBarFilterKeywords(value: String) {
        _videoOptionBarFilterKeywords.value = value
        config.videoOptionBarFilterKeywords = value
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

    // 弹窗过滤
    fun changeIsDialogFilter(value: Boolean) {
        _isDialogFilter.value = value
        config.isDialogFilter = value
    }

    // 弹窗关闭提示
    fun setDialogDismissTips(value: Boolean) {
        _dialogDismissTips.value = value
        config.dialogDismissTips = value
    }

    // 弹窗过滤关键字
    fun setDialogFilterKeywords(value: String) {
        _dialogFilterKeywords.value = value
        config.dialogFilterKeywords = value
    }

    // 清爽模式
    fun changeIsNeatMode(value: Boolean) {
        _isNeatMode.value = value
        config.isNeatMode = value
    }

    // 全屏沉浸
    fun changeIsImmersive(value: Boolean) {
        _isImmersive.value = value
        config.isImmersive = value
    }

    // 系统隐藏项(状态栏、导航栏)
    fun setSystemControllerValue(value: List<Boolean>) {
        _systemControllerValue.value = value
        config.systemControllerValue = value
    }

    // 清爽模式弹窗响应模式
    fun setLongPressMode(value: Boolean) {
        _longPressMode.value = value
        config.longPressMode = value
    }

    // 评论区颜色模式
    fun changeIsCommentColorMode(value: Boolean) {
        _isCommentColorMode.value = value
        config.isCommentColorMode = value
    }

    // 评论区颜色模式
    fun changeCommentColorMode(value: Int) {
        _commentColorMode.value = value
        config.commentColorMode = value
    }

    // WebDav
    fun changeIsWebDav(value: Boolean) {
        _isWebDav.value = value
        config.isWebDav = value
    }

    // 初始化WebDav
    fun testWebDav(block: (Boolean, String) -> Unit) {
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
                    webDav.put(
                        "数据目录，谨慎删除.txt",
                        "Freedom",
                        bytes = "该目录为Freedom数据目录，请谨慎删除!!!".toByteArray()
                    )
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
    fun setTimedShutdownValue(value: List<Int>) {
        _timedShutdownValue.value = value
        config.timedShutdownValue = value
    }

    // 保留应用后台
    fun setKeepAppBackend(value: Boolean) {
        _keepAppBackend.value = value
        config.keepAppBackend = value
    }

    fun changeIsCrashTolerance(value: Boolean) {
        _isCrashTolerance.value = value
        config.isCrashTolerance = value
    }

    // 去插件化
    fun changeIsDisablePlugin(value: Boolean) {
        _isDisablePlugin.value = value
        config.isDisablePlugin = value
    }

    // 保存版本信息
    fun setVersionConfig(asset: AssetManager?) {
        if (asset == null) {
            config.versionConfig = config.versionConfig.copy(
                dyVersionName = app.appVersionName,
                dyVersionCode = app.appVersionCode,
            )
            return
        }

        val version = asset.readAssetsAsText("version")
        val versionName = version.substringBeforeLast("-")
        val versionCode = version.substringAfterLast("-")
        config.versionConfig = config.versionConfig.copy(
            versionName,
            versionCode.toLong(),
            app.appVersionName,
            app.appVersionCode,
        )
    }

    val hasDexkitCache get() = !config.dexkitCache.isEmpty

    // 清除类日志
    fun clearDexkitCache() {
        config.dexkitCache = JSONObject()
    }
}