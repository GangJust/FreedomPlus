package io.github.fplus.core.hook.logic

import com.freegang.extension.format
import com.freegang.extension.getStringOrDefault
import com.freegang.extension.parseJSON
import com.freegang.ktutils.net.KHttpUtils
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.ugc.aweme.ImageUrlStruct
import io.github.xpler.core.XplerLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

fun Aweme.createDate(): String {
    return Date(this.createTime * 1000).format()
}

suspend fun Aweme.cityInfo(): String {
    return withContext(Dispatchers.IO) {
        runCatching {
            val result = KHttpUtils.get("http://api.ip33.com/Area_Code/GetArea/?code=$city")
            val areaJson = result.parseJSON()
            val province = areaJson.getStringOrDefault("province")
            val city = areaJson.getStringOrDefault("city")
            val county = areaJson.getStringOrDefault("county")

            "$province $city $county".trim()
        }.getOrElse {
            XplerLog.e(it)
            "获取失败"
        }
    }
}

fun Aweme.getH265VideoUrlList(): List<String> {
    val video = video ?: return emptyList()
    return video.playAddrH265?.urlList ?: emptyList()
}

fun Aweme.getH264VideoUrlList(): List<String> {
    val video = video ?: return emptyList()
    return video.h264PlayAddr?.urlList ?: emptyList()
}

fun Aweme.getAutoVideoUrlList(): List<String> {
    val video = video ?: return emptyList()
    return video.playAddr?.urlList ?: emptyList()
}

fun Aweme.getImageUrlList(): List<ImageUrlStruct> {
    return images ?: return emptyList()
}

fun Aweme.getMusicUrlList(): List<String> {
    val music = music ?: return emptyList()
    return music.playUrl?.urlList ?: emptyList()
}

fun Aweme.sortString(): String {
    val desc = "$desc".replace(Regex("\\s"), "")
    return "awemeType=${awemeType}, desc=$desc"
}