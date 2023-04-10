package com.freegang.douyin.model

data class DeAweme(
    var desc: String = "",
    var nickname: String = "",
    var shortId: String = "",
    var videoUrlList: List<String> = listOf(),
    var musicUrlList: List<String> = listOf(),
    var imageUrlStructList: List<UrlStruct> = listOf(),
) {
    fun isEmpty(): Boolean {
        return desc.isEmpty()
                && nickname.isEmpty()
                && shortId.isEmpty()
                && videoUrlList.isEmpty()
                && musicUrlList.isEmpty()
                && imageUrlStructList.isEmpty()
    }
}

data class UrlStruct(
    val urlList: List<String>,
)