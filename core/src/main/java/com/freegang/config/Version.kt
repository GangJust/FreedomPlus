package com.freegang.config

import android.util.Base64
import com.freegang.ktutils.json.firstJsonObject
import com.freegang.ktutils.json.getLongOrDefault
import com.freegang.ktutils.json.getStringOrDefault
import com.freegang.ktutils.json.parseJSON
import com.freegang.ktutils.net.KHttpUtils


object Version {
    // Api
    private const val githubReleasesApi = "https://api.github.com/repos/GangJust/FreedomPlus/releases/latest"

    // 获取Github最后一次 releases
    fun getRemoteReleasesLatest(): VersionConfig? {
        val get = KHttpUtils.get(githubReleasesApi)
        if (get.isEmpty()) return null
        if (!get.contains("browser_download_url")) return null
        return parseVersionConfig(get)
    }

    // 解析出版本信息
    private fun parseVersionConfig(s: String): VersionConfig {
        val json = s.parseJSON()
        return VersionConfig(
            htmlUrl = json.getStringOrDefault("html_url"),
            tagName = json.getStringOrDefault("tag_name"),
            name = json.getStringOrDefault("name"),
            body = json.getStringOrDefault("body"),
            size = json.getJSONArray("assets").firstJsonObject().getLongOrDefault("size"),
            createdAt = json.getJSONArray("assets").firstJsonObject().getStringOrDefault("created_at"),
            updatedAt = json.getJSONArray("assets").firstJsonObject().getStringOrDefault("updated_at"),
            browserDownloadUrl = json.getJSONArray("assets").firstJsonObject().getStringOrDefault("browser_download_url"),
        )
    }

    // 获取适配版本列表
    fun getVersions(): String {
        //if (null == null) return "null" //一般用作逻辑阻断
        //return KHttpUtils.get("https://raw.githubusercontent.com/GangJust/FreedomPlus/master/versions.json")
        val body = KHttpUtils.get("https://api.github.com/repos/GangJust/FreedomPlus/contents/versions.json?ref=master")
        return Base64.decode(body.parseJSON().getStringOrDefault("content"), Base64.DEFAULT).toString(Charsets.UTF_8)
    }
}

// 版本信息
data class VersionConfig(
    val htmlUrl: String,
    val tagName: String,
    val name: String,
    val body: String,
    val size: Long,
    val createdAt: String,
    val updatedAt: String,
    val browserDownloadUrl: String,
)