package com.freegang.config

import com.freegang.xpler.utils.json.KJSONUtils.firstJsonObject
import com.freegang.xpler.utils.json.KJSONUtils.getLongOrDefault
import com.freegang.xpler.utils.json.KJSONUtils.getStringOrDefault
import com.freegang.xpler.utils.json.KJSONUtils.parseJSON
import com.freegang.xpler.utils.net.KHttpUtils

object Version {
    // Api
    private const val githubReleasesApi = "https://api.github.com/repos/GangJust/FreedomPlus/releases/latest"
    //private const val githubReleasesApi = "https://api.github.com/repos/GangJust/Freedom/releases/latest"

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