package com.freegang.config

import android.content.Context
import android.os.Environment
import android.util.JsonWriter
import com.freegang.xpler.utils.io.KFileUtils.child
import com.freegang.xpler.utils.io.KFileUtils.need
import com.freegang.xpler.utils.io.KStorageUtils.storageRootFile
import com.freegang.xpler.utils.json.KJSONUtils
import com.freegang.xpler.utils.json.KJSONUtils.getBooleanOrDefault
import com.freegang.xpler.utils.json.KJSONUtils.getLongOrDefault
import com.freegang.xpler.utils.json.KJSONUtils.getStringOrDefault
import java.io.File
import java.io.FileWriter

data class Config(
    var isSupportHint: Boolean = true, //是否显示兼容
    var isOwnerDir: Boolean = false, //是否按视频创作者单独创建文件夹
    var isDownload: Boolean = false, //是否开启视频/图文/音乐下载
    var isEmoji: Boolean = false, //是否开启评论区图片/表情包保存
    var isHideTab: Boolean = false, //是否开启隐藏顶部tab
    var hideTabKeyword: String = "探索, 商城", //隐藏顶部tab包含的关键字, 逗号隔开
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
                isHideTab = json.getBooleanOrDefault("isHideTab"),
                hideTabKeyword = json.getStringOrDefault("hideTabKeyword"),
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
            .name("isHideTab").value(isHideTab)
            .name("hideTabKeyword").value(hideTabKeyword)
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
