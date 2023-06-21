package com.freegang.config

import android.content.Context
import android.os.Environment
import android.util.JsonWriter
import com.freegang.ktutils.io.child
import com.freegang.ktutils.io.need
import com.freegang.ktutils.io.storageRootFile
import com.freegang.ktutils.json.KJSONUtils
import com.freegang.ktutils.json.getBooleanOrDefault
import com.freegang.ktutils.json.getLongOrDefault
import com.freegang.ktutils.json.getStringOrDefault
import java.io.File
import java.io.FileWriter

data class Config(
    var isSupportHint: Boolean = true, //是否显示兼容
    var isOwnerDir: Boolean = false, //是否按视频创作者单独创建文件夹
    var isDownload: Boolean = false, //是否开启视频/图文/音乐下载
    var isEmoji: Boolean = false, //是否开启表情包保存
    var isVibrate: Boolean = false, //是否开启震动反馈
    var isTranslucent: Boolean = false, //是否开启首页控件半透明
    var isNeat: Boolean = false, //是否开启清爽模式
    var isLongPressMode: Boolean = false, //清爽模式弹窗响应模式 true上半, false下半
    var neatState: Boolean = false, //当前是否处于清爽模式
    var isNotification: Boolean = false, //是否通知栏下载
    var isWebDav: Boolean = false, //是否开启WebDav
    var webDavHost: String = "", //WebDav地址
    var webDavUsername: String = "", //WebDav用户名
    var webDavPassword: String = "", //WebDav密码
    var isHideTab: Boolean = false, //是否开启隐藏顶部tab
    var hideTabKeywords: String = "经验, 探索, 商城", //隐藏顶部tab包含的关键字, 逗号隔开
    var versionName: String = "", //版本名称
    var versionCode: Long = 0L, //版本代码
    var dyVersionName: String = "", //抖音版本名称
    var dyVersionCode: Long = 0L, //抖音版本代码
) {
    companion object {
        private var config: Config? = null

        private fun getSettingFile(context: Context): File {
            return getConfigDir(context)
                .child("setting.json")
                .need(true)
        }

        fun read(context: Context): Config {
            val settingFile = getSettingFile(context)

            val setting = settingFile.readText()
            if (setting.isBlank()) return Config()

            val json = KJSONUtils.parse(setting)
            config = Config(
                isSupportHint = json.getBooleanOrDefault("isSupportHint"),
                isOwnerDir = json.getBooleanOrDefault("isOwnerDir"),
                isDownload = json.getBooleanOrDefault("isDownload"),
                isEmoji = json.getBooleanOrDefault("isEmoji"),
                isVibrate = json.getBooleanOrDefault("isVibrate"),
                isTranslucent = json.getBooleanOrDefault("isTranslucent"),
                isNeat = json.getBooleanOrDefault("isNeat"),
                isLongPressMode = json.getBooleanOrDefault("isLongPressMode"),
                neatState = json.getBooleanOrDefault("neatState"),
                isNotification = json.getBooleanOrDefault("isNotification"),
                isWebDav = json.getBooleanOrDefault("isWebDav"),
                webDavHost = json.getStringOrDefault("webDavHost"),
                webDavUsername = json.getStringOrDefault("webDavUsername"),
                webDavPassword = json.getStringOrDefault("webDavPassword"),
                isHideTab = json.getBooleanOrDefault("isHideTab"),
                hideTabKeywords = json.getStringOrDefault("hideTabKeywords"),
                versionName = json.getStringOrDefault("versionName"),
                versionCode = json.getLongOrDefault("versionCode"),
                dyVersionName = json.getStringOrDefault("dyVersionName"),
                dyVersionCode = json.getLongOrDefault("dyVersionCode"),
            )

            return config!!
        }

        fun get(): Config {
            if (config == null) throw Exception("模块配置未加载, 请先调用read方法!")
            return config!!
        }

        fun getConfigDir(context: Context): File {
            return getFreedomDir(context)
                .child(".config")
        }

        fun getFreedomDir(context: Context): File {
            return context.applicationContext.storageRootFile
                .child(Environment.DIRECTORY_DCIM)
                .child("Freedom")
        }
    }

    fun save(context: Context) {
        val settingFile = getSettingFile(context)

        val jsonWriter = JsonWriter(FileWriter(settingFile))
            .beginObject()
            .name("isSupportHint").value(isSupportHint)
            .name("isOwnerDir").value(isOwnerDir)
            .name("isDownload").value(isDownload)
            .name("isEmoji").value(isEmoji)
            .name("isVibrate").value(isVibrate)
            .name("isTranslucent").value(isTranslucent)
            .name("isNeat").value(isNeat)
            .name("isLongPressMode").value(isLongPressMode)
            .name("neatState").value(neatState)
            .name("isNotification").value(isNotification)
            .name("isWebDav").value(isWebDav)
            .name("webDavHost").value(webDavHost)
            .name("webDavUsername").value(webDavUsername)
            .name("webDavPassword").value(webDavPassword)
            .name("isHideTab").value(isHideTab)
            .name("hideTabKeywords").value(hideTabKeywords)
            .name("versionName").value(versionName)
            .name("versionCode").value(versionCode)
            .name("dyVersionName").value(dyVersionName)
            .name("dyVersionCode").value(dyVersionCode)
            .endObject()
        jsonWriter.flush()
        jsonWriter.close()
    }

    fun remove(context: Context) {
        getSettingFile(context).delete()
    }
}
