package com.freegang.douyin.model

data class DeAweme(
    var desc: String = "",
    var nickname: String = "",
    var shortId: String = "",
    var videoUrlList: List<String> = listOf(),
    var imageUrlStructList: List<ImageUrlStruct> = listOf(),
    var musicUrlList: List<String> = listOf(),
) {
    fun isEmpty(): Boolean {
        return desc.isEmpty()
                && nickname.isEmpty()
                && shortId.isEmpty()
                && videoUrlList.isEmpty()
                && imageUrlStructList.isEmpty()
                && musicUrlList.isEmpty()
    }
}

data class ImageUrlStruct(
    val imageUrlList: List<String>,
)