package com.freegang.config

import android.content.Context
import android.os.Environment
import com.freegang.ktutils.io.child
import com.freegang.ktutils.io.storageRootFile
import com.freegang.ktutils.json.getStringOrDefault
import com.freegang.ktutils.json.parseJSON
import com.freegang.webdav.WebDav
import com.tencent.mmkv.MMKV
import java.io.File

class ConfigV1 private constructor() {
    data class Version(
        var versionName: String = "", //版本名称
        var versionCode: Long = 0L, //版本代码
        var dyVersionName: String = "", //抖音版本名称
        var dyVersionCode: Long = 0L, //抖音版本代码
    )

    companion object {
        private val mmkv by lazy { MMKV.defaultMMKV() }

        private val config by lazy { ConfigV1() }

        fun getFreedomDir(context: Context): File {
            return context.applicationContext.storageRootFile
                .child(Environment.DIRECTORY_DCIM)
                .child("Freedom")
        }

        fun getConfigDir(context: Context): File {
            return getFreedomDir(context)
                .child(".config")
        }

        fun initialize(context: Context) {
            MMKV.initialize(context, getConfigDir(context).absolutePath)
        }

        fun get() = config
    }

    /// 是否按视频创作者单独创建文件夹
    var isOwnerDir: Boolean = false
        get() {
            field = mmkv.getBoolean("isOwnerDir", false)
            return field
        }
        set(value) {
            mmkv.putBoolean("isOwnerDir", value)
            field = value
        }

    /// 是否开启视频/图文/音乐下载
    var isDownload: Boolean = false
        get() {
            field = mmkv.getBoolean("isDownload", false)
            return field
        }
        set(value) {
            mmkv.putBoolean("isDownload", value)
            field = value
        }

    /// 是否开启表情包保存
    var isEmoji: Boolean = false
        get() {
            field = mmkv.getBoolean("isEmoji", false)
            return field
        }
        set(value) {
            mmkv.putBoolean("isEmoji", value)
            field = value
        }

    /// 是否开启震动反馈
    var isVibrate: Boolean = false
        get() {
            field = mmkv.getBoolean("isVibrate", false)
            return field
        }
        set(value) {
            mmkv.putBoolean("isVibrate", value)
            field = value
        }

    /// 是否开启首页控件半透明
    var isTranslucent: Boolean = false
        get() {
            field = mmkv.getBoolean("isTranslucent", false)
            return field
        }
        set(value) {
            mmkv.putBoolean("isTranslucent", value)
            field = value
        }

    /// 是否禁用双击点赞
    var isDisableDoubleLike: Boolean = false
        get() {
            field = mmkv.getBoolean("isDisableDoubleLike", false)
            return field
        }
        set(value) {
            mmkv.putBoolean("isDisableDoubleLike", value)
            field = value
        }

    /// 是否开启清爽模式
    var isNeatMode: Boolean = false
        get() {
            field = mmkv.getBoolean("isNeatMode", false)
            return field
        }
        set(value) {
            mmkv.putBoolean("isNeatMode", value)
            field = value
        }

    /// 当前是否处于清爽模式
    var neatModeState: Boolean = false
        get() {
            field = mmkv.getBoolean("neatModeState", false)
            return field
        }
        set(value) {
            mmkv.putBoolean("neatModeState", value)
            field = value
        }

    /// 清爽模式弹窗响应模式 true上半, false下半
    var longPressMode: Boolean = false
        get() {
            field = mmkv.getBoolean("longPressMode", false)
            return field
        }
        set(value) {
            mmkv.putBoolean("longPressMode", value)
            field = value
        }

    /// 是否通知栏下载
    var isNotification: Boolean = false
        get() {
            field = mmkv.getBoolean("isNotification", false)
            return field
        }
        set(value) {
            mmkv.putBoolean("isNotification", value)
            field = value
        }

    /// 是否开启WebDav
    var isWebDav: Boolean = false
        get() {
            field = mmkv.getBoolean("isWebDav", false)
            return field
        }
        set(value) {
            mmkv.putBoolean("isWebDav", value)
            field = value
        }

    /// WebDav 配置
    var webDavConfig: WebDav.Config = WebDav.Config()
        get() {
            return WebDav.Config(
                mmkv.getString("webDavHost", "")!!,
                mmkv.getString("webDavUsername", "")!!,
                mmkv.getString("webDavPassword", "")!!,
            )
        }
        set(value) {
            field = value
            mmkv.putString("webDavHost", field.host)
            mmkv.putString("webDavUsername", field.username)
            mmkv.putString("webDavPassword", field.password)
        }

    /// WebDav 历史
    val webDavConfigList: List<WebDav.Config>
        get() {
            val set = mmkv.getStringSet("webDavHistory", emptySet())!!
            return set.map {
                val json = it.parseJSON()
                WebDav.Config(
                    host = json.getStringOrDefault("host"),
                    username = json.getStringOrDefault("username"),
                    password = json.getStringOrDefault("password"),
                )
            }
        }

    ///
    fun addWebDavConfig(config: WebDav.Config) {
        val set = mmkv.getStringSet("webDavHistory", mutableSetOf())!!
        set.add(config.toJson())
        mmkv.putStringSet("webDavHistory", set)
    }

    ///
    fun removeWebDavConfig(config: WebDav.Config) {
        val set = mmkv.getStringSet("webDavHistory", mutableSetOf())!!
        set.remove(config.toJson())
        mmkv.putStringSet("webDavHistory", set)
    }

    /// 是否开启隐藏顶部tab
    var isHideTab: Boolean = false
        get() {
            field = mmkv.getBoolean("isHideTab", false)
            return field
        }
        set(value) {
            mmkv.putBoolean("isHideTab", value)
            field = value
        }

    /// 隐藏顶部tab包含的关键字, 逗号隔开
    var hideTabKeywords: String = "经验, 探索, 商城"
        get() {
            field = mmkv.getString("hideTabKeywords", "经验, 探索, 商城")!!
            return field
        }
        set(value) {
            mmkv.putString("hideTabKeywords", value)
            field = value
        }

    /// 定时退出
    var isTimedExit: Boolean = false
        get() {
            field = mmkv.getBoolean("isTimedExit", false)!!
            return field
        }
        set(value) {
            mmkv.putBoolean("isTimedExit", value)
            field = value
        }

    /// 定时退出[运行时间, 空闲时间]
    var timedExitValue: String = "[10, 3]"
        get() {
            field = mmkv.getString("timedExitValue", "[10, 3]")!!
            return field
        }
        set(value) {
            mmkv.putString("timedExitValue", value)
            field = value
        }

    /// 去插件化
    var isDisablePlugin: Boolean = false
        get() {
            field = mmkv.getBoolean("isDisablePlugin", false)!!
            return field
        }
        set(value) {
            mmkv.putBoolean("isDisablePlugin", value)
            field = value
        }

    /// 版本信息
    var versionConfig: ConfigV1.Version = ConfigV1.Version()
        get() {
            return Version(
                mmkv.getString("versionName", "")!!,
                mmkv.getLong("versionCode", 0L),
                mmkv.getString("dyVersionName", "")!!,
                mmkv.getLong("dyVersionCode", 0L),
            )
        }
        set(value) {
            field = value
            mmkv.putString("versionName", value.versionName)
            mmkv.putLong("versionCode", value.versionCode)
            mmkv.putString("dyVersionName", value.dyVersionName)
            mmkv.putLong("dyVersionCode", value.dyVersionCode)
        }
}
